// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页响应
export interface PageResponse<T> {
  list: T[]
  pagination: {
    page: number
    pageSize: number
    total: number
    totalPages: number
  }
}

// 分页请求参数
export interface PageParams {
  page?: number
  pageSize?: number
}
