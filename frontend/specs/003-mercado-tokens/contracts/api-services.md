# API Services Contract

**Feature**: 003-mercado-tokens | **Phase**: 1 — Design | **Date**: 2026-11-16

Este documento define el contrato de los módulos de servicio que consumen el backend. Los componentes deben acceder a la API únicamente a través de estos servicios. El cliente compartido `openapi-fetch` y los tipos generados desde `docs/openapi.yaml` son la fuente de verdad para rutas, parámetros, cuerpos y respuestas.

## Cliente HTTP compartido

El cliente vive en `src/api/client.ts`, usa `openapi-fetch` parametrizado con el tipo `paths` generado, y agrega el JWT mediante middleware de request. Los servicios reciben las respuestas discriminadas de `openapi-fetch` (`data`, `error` y `response`) y propagan errores explícitamente usando el helper `requireApiData` del cliente.

```ts
// src/api/client.ts
import createClient from 'openapi-fetch'
import type { paths } from './schema'

export const apiClient = createClient<paths>({
  baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

apiClient.use({
  onRequest({ request }) {
    const token = window.localStorage.getItem('authToken')
    if (token) {
      request.headers.set('Authorization', `Bearer ${token}`)
    }
    return request
  },
})
```

`requireApiData<T>(data, error, response, fallbackMessage)` comprueba el estado HTTP y la presencia de datos, y lanza `ApiRequestError` ante respuestas fallidas o inesperadas. Para operaciones sin cuerpo de respuesta (por ejemplo, cancelar una orden), el servicio debe comprobar `error` y `response.ok` explícitamente. No se debe crear otro cliente HTTP ni realizar llamadas de red directamente desde componentes.

> **Consistencia de tipos OpenAPI**: los nombres de `params.path` deben coincidir con los placeholders de cada ruta (`{id}`, `{playerId}`, `{userId}`, `{usuarioId}`). `GET /users/{id}` conserva su parámetro `id`; las valuaciones usan un parámetro dedicado `UserIdQuotes` cuyo nombre es `userId`. Regenerar `src/api/schema.d.ts` desde `docs/openapi.yaml` y no debilitar los tipos con casts.

## userService.ts

```ts
// src/services/userService.ts
import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

type UserResponse = components['schemas']['UserResponse']

export const getUserProfile = async (userId: number): Promise<UserResponse> => {
  const { data, error, response } = await apiClient.GET('/users/{id}', {
    params: { path: { id: userId } },
  })

  return requireApiData(data, error, response, 'No se pudo obtener el perfil.')
}
```

## playerService.ts

Los métodos existentes de consulta de jugadores se conservan. Se agregan estas operaciones de valuación:

```ts
// src/services/playerService.ts — métodos de valuación
import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

type ValuationResponse = components['schemas']['ValuationResponse']

export const getPlayerValuations = async (
  playerId: number,
  userId: number,
): Promise<ValuationResponse[]> => {
  const { data, error, response } = await apiClient.GET(
    '/players/{playerId}/quotes/{userId}',
    { params: { path: { playerId, userId } } },
  )

  return requireApiData(data, error, response, 'No se pudieron obtener las valuaciones.')
}

export const getCurrentValuation = async (
  playerId: number,
  userId: number,
): Promise<ValuationResponse> => {
  const { data, error, response } = await apiClient.GET(
    '/players/{playerId}/quotes/{userId}/current',
    { params: { path: { playerId, userId } } },
  )

  return requireApiData(data, error, response, 'No se pudo obtener la valuación actual.')
}

export const getClosestValuation = async (
  playerId: number,
  userId: number,
  date: string,
): Promise<ValuationResponse> => {
  const { data, error, response } = await apiClient.GET(
    '/players/{playerId}/quotes/{userId}/closest',
    { params: { path: { playerId, userId }, query: { date } } },
  )

  return requireApiData(data, error, response, 'No se pudo obtener la valuación solicitada.')
}
```

## orderService.ts

```ts
// src/services/orderService.ts
import type { components } from '../api/schema'
import { ApiRequestError, apiClient, requireApiData } from '../api/client'

type CrearOrdenRequest = components['schemas']['CrearOrdenRequest']
type OrdenResponse = components['schemas']['OrdenResponse']
type OrdenPendienteResponse = components['schemas']['OrdenPendienteResponse']
type TransaccionEjecutadaResponse = components['schemas']['TransaccionEjecutadaResponse']

export const createBuyOrder = async (
  orderData: CrearOrdenRequest,
): Promise<OrdenResponse> => {
  const { data, error, response } = await apiClient.POST('/orders/buy', {
    body: orderData,
  })

  return requireApiData(data, error, response, 'No se pudo crear la orden de compra.')
}

export const createSellOrder = async (
  orderData: CrearOrdenRequest,
): Promise<OrdenResponse> => {
  const { data, error, response } = await apiClient.POST('/orders/sell', {
    body: orderData,
  })

  return requireApiData(data, error, response, 'No se pudo crear la orden de venta.')
}

export const getPendingOrders = async (
  userId: number,
): Promise<OrdenPendienteResponse[]> => {
  const { data, error, response } = await apiClient.GET('/users/{usuarioId}/orders', {
    params: { path: { usuarioId: userId } },
  })

  return requireApiData(data, error, response, 'No se pudieron obtener las órdenes.')
}

export const getTransactions = async (
  userId: number,
): Promise<TransaccionEjecutadaResponse[]> => {
  const { data, error, response } = await apiClient.GET(
    '/users/{usuarioId}/transactions',
    { params: { path: { usuarioId: userId } } },
  )

  return requireApiData(data, error, response, 'No se pudieron obtener las transacciones.')
}

export const cancelOrder = async (orderId: number, userId: number): Promise<void> => {
  const { error, response } = await apiClient.DELETE('/orders/{orderId}', {
    params: { path: { orderId }, query: { usuarioId: userId } },
  })

  if (error || !response.ok) {
    throw new ApiRequestError('No se pudo cancelar la orden.', response.status)
  }
}
```

## strategyService.ts

```ts
// src/services/strategyService.ts
import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

type StrategyStateResponse = components['schemas']['StrategyStateResponse']
type StrategyRequest = components['schemas']['StrategyRequest']

export const getStrategy = async (userId: number): Promise<StrategyStateResponse> => {
  const { data, error, response } = await apiClient.GET('/strategy/{usuarioId}', {
    params: { path: { usuarioId: userId } },
  })

  return requireApiData(data, error, response, 'No se pudo obtener la estrategia.')
}

export const configureStrategy = async (
  userId: number,
  strategyData: StrategyRequest,
): Promise<StrategyStateResponse> => {
  const { data, error, response } = await apiClient.PUT(
    '/strategy/{usuarioId}/configure',
    { params: { path: { usuarioId: userId } }, body: strategyData },
  )

  return requireApiData(data, error, response, 'No se pudo configurar la estrategia.')
}
```

## holdingService.ts

```ts
// src/services/holdingService.ts
import type { components } from '../api/schema'
import { apiClient, requireApiData } from '../api/client'

type HoldingResponse = components['schemas']['HoldingResponse']

export const getHoldings = async (userId: number): Promise<HoldingResponse[]> => {
  const { data, error, response } = await apiClient.GET('/users/{usuarioId}/holdings', {
    params: { path: { usuarioId: userId } },
  })

  return requireApiData(data, error, response, 'No se pudieron obtener las tenencias.')
}
```
