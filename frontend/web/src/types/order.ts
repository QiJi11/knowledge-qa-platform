// 订单状态
export enum OrderStatus {
  PENDING = 0,    // 待支付
  PAID = 1,       // 已支付
  CANCELLED = 2,  // 已取消
  REFUNDED = 3    // 已退款
}

// 订单信息
export interface Order {
  id: number
  orderNo: string
  userId: number
  courseId: number
  course?: {
    id: number
    title: string
    coverUrl: string
    price: number
  }
  amount: number
  status: OrderStatus
  payTime?: string
  createdAt: string
  updatedAt: string
}

// 创建订单参数
export interface CreateOrderParams {
  courseId: number
}

// 支付参数
export interface PayOrderParams {
  orderNo: string
  payType: 'alipay' | 'wechat'
}
