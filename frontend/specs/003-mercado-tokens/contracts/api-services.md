# API Services Contract

**Feature**: 003-mercado-tokens | **Phase**: 1 — Design | **Date**: 2026-11-16

Este documento define el contrato público de los módulos de servicio que el frontend expone a los componentes. Cada función es la única interfaz permitida para consumir el backend (principio de Modular API Consumption de la constitución).

---

## apiClient.js (actualizado)

Añadir interceptor de request para adjuntar JWT automáticamente:

```js
// src/services/apiClient.js
import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Interceptor: adjuntar Authorization header si hay token en localStorage
apiClient.interceptors.request.use((config) => {
  const token = window.localStorage.getItem('authToken')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

export default apiClient
```

---

## userService.js (NUEVO)

```js
// src/services/userService.js
import apiClient from './apiClient'

/**
 * Obtiene el perfil de un usuario por ID.
 * @param {number} userId
 * @returns {Promise<{ nombre: string }>}
 */
export const getUserProfile = async (userId) => {
  const response = await apiClient.get(`/users/${userId}`)
  return response.data
}
```

---

## playerService.js (actualizado)

Mantiene `fetchPlayers`/`getPlayers` existentes. Añade:

```js
// src/services/playerService.js — adiciones

/**
 * Obtiene todas las valuaciones de un jugador para un usuario.
 * @param {number} playerId
 * @param {number} userId
 * @returns {Promise<ValuationResponse[]>}
 */
export const getPlayerValuations = async (playerId, userId) => {
  const response = await apiClient.get(`/players/${playerId}/quotes/${userId}`)
  return response.data
}

/**
 * Obtiene la valuación actual de un jugador para un usuario.
 * @param {number} playerId
 * @param {number} userId
 * @returns {Promise<ValuationResponse>}
 */
export const getCurrentValuation = async (playerId, userId) => {
  const response = await apiClient.get(`/players/${playerId}/quotes/${userId}/current`)
  return response.data
}

/**
 * Obtiene la valuación más cercana a una fecha.
 * @param {number} playerId
 * @param {number} userId
 * @param {string} date — ISO 8601 date-time string
 * @returns {Promise<ValuationResponse>}
 */
export const getClosestValuation = async (playerId, userId, date) => {
  const response = await apiClient.get(`/players/${playerId}/quotes/${userId}/closest`, {
    params: { date },
  })
  return response.data
}
```

---

## orderService.js (NUEVO)

```js
// src/services/orderService.js
import apiClient from './apiClient'

/**
 * Crea una orden de compra.
 * Requiere Authorization header (adjuntado por interceptor de apiClient).
 * @param {CrearOrdenRequest} orderData
 * @returns {Promise<OrdenResponse>}
 */
export const createBuyOrder = async (orderData) => {
  const response = await apiClient.post('/orders/buy', orderData)
  return response.data
}

/**
 * Crea una orden de venta.
 * Requiere Authorization header (adjuntado por interceptor de apiClient).
 * @param {CrearOrdenRequest} orderData
 * @returns {Promise<OrdenResponse>}
 */
export const createSellOrder = async (orderData) => {
  const response = await apiClient.post('/orders/sell', orderData)
  return response.data
}

/**
 * Obtiene las órdenes pendientes de un usuario.
 * @param {number} userId
 * @returns {Promise<OrdenPendienteResponse[]>}
 */
export const getPendingOrders = async (userId) => {
  const response = await apiClient.get(`/users/${userId}/orders`)
  return response.data
}

/**
 * Obtiene las transacciones ejecutadas de un usuario.
 * @param {number} userId
 * @returns {Promise<TransaccionEjecutadaResponse[]>}
 */
export const getTransactions = async (userId) => {
  const response = await apiClient.get(`/users/${userId}/transactions`)
  return response.data
}

/**
 * Cancela una orden pendiente.
 * @param {number} orderId
 * @param {number} userId
 * @returns {Promise<void>}
 */
export const cancelOrder = async (orderId, userId) => {
  await apiClient.delete(`/orders/${orderId}`, { params: { usuarioId: userId } })
}
```

---

## strategyService.js (NUEVO)

```js
// src/services/strategyService.js
import apiClient from './apiClient'

/**
 * Obtiene el estado actual de la estrategia de valuación de un usuario.
 * @param {number} userId
 * @returns {Promise<StrategyStateResponse>}
 */
export const getStrategy = async (userId) => {
  const response = await apiClient.get(`/strategy/${userId}`)
  return response.data
}

/**
 * Configura (crea o actualiza) la estrategia de valuación de un usuario.
 * @param {number} userId
 * @param {StrategyRequest} strategyData
 * @returns {Promise<StrategyStateResponse>}
 */
export const configureStrategy = async (userId, strategyData) => {
  const response = await apiClient.put(`/strategy/${userId}/configure`, strategyData)
  return response.data
}
```

---

## holdingService.js (NUEVO)

```js
// src/services/holdingService.js
import apiClient from './apiClient'

/**
 * Obtiene las tenencias actuales de un usuario.
 * @param {number} userId
 * @returns {Promise<HoldingResponse[]>}
 */
export const getHoldings = async (userId) => {
  const response = await apiClient.get(`/users/${userId}/holdings`)
  return response.data
}
```
