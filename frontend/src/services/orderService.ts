import type { components } from '../api/schema'
import { ApiRequestError, apiClient, requireApiData } from '../api/client'

export type CreateOrderRequest = components['schemas']['CrearOrdenRequest']
export type PendingOrder = components['schemas']['OrdenPendienteResponse']
export type Transaction = components['schemas']['TransaccionEjecutadaResponse']

export const createBuyOrder = async (order: CreateOrderRequest) => {
  const { data, error, response } = await apiClient.POST('/orders/buy', { body: order })
  return requireApiData(data, error, response, 'No se pudo crear la orden de compra.')
}

export const createSellOrder = async (order: CreateOrderRequest) => {
  const { data, error, response } = await apiClient.POST('/orders/sell', { body: order })
  return requireApiData(data, error, response, 'No se pudo crear la orden de venta.')
}

export const getPendingOrders = async (userId: number): Promise<PendingOrder[]> => {
  const { data, error, response } = await apiClient.GET('/users/{usuarioId}/orders', {
    params: { path: { usuarioId: userId } },
  })

  return requireApiData(data, error, response, 'No se pudieron obtener las órdenes pendientes.')
}

export const getTransactions = async (userId: number): Promise<Transaction[]> => {
  const { data, error, response } = await apiClient.GET('/users/{usuarioId}/transactions', {
    params: { path: { usuarioId: userId } },
  })

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
