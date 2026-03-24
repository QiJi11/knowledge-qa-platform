import { request } from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/common'
import type { Order, CreateOrderParams, PayOrderParams } from '@/types/order'

// 创建订单
export function createOrder(data: CreateOrderParams) {
  return request.post<ApiResponse<Order>>('/order/create', data)
}

// 获取订单详情
export function getOrderDetail(orderNo: string) {
  return request.get<ApiResponse<Order>>(`/order/${orderNo}`)
}

// 模拟支付
export function payOrder(data: PayOrderParams) {
  return request.post<ApiResponse<{ success: boolean }>>('/order/pay', data)
}

// 获取订单列表
export function getOrderList(params?: { page?: number; pageSize?: number; status?: number }) {
  return request.get<ApiResponse<PageResponse<Order>>>('/order/list', { params })
}

// 取消订单
export function cancelOrder(orderNo: string) {
  return request.post<ApiResponse<void>>(`/order/${orderNo}/cancel`)
}
