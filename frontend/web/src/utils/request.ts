import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

// 创建axios实例
const service: AxiosInstance = axios.create({
  // 默认走 /api/v1（与 backend-java 的 API 前缀一致），并由 Vite proxy 转发到 http://localhost:3001
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动携带 token
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 业务错误处理
    if (typeof res?.code !== 'number') {
      return Promise.reject(new Error('后端返回格式不符合预期'))
    }

    if (res.code !== 0) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    // HTTP错误处理
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

// 封装请求方法
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.patch(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  }
}

export default service
