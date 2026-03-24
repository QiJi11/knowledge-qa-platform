import { request } from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/common'
import type { UserInfo, LoginParams, RegisterParams, LoginResponse } from '@/types/user'

// 用户注册
export function register(data: RegisterParams) {
  return request.post<ApiResponse<UserInfo>>('/user/register', data)
}

// 用户登录
export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResponse>>('/user/login', data)
}

// 发送验证码
export function sendCode(phone: string) {
  return request.post<ApiResponse<void>>('/user/send-code', { phone })
}

// 获取用户信息
export function getUserInfo() {
  return request.get<ApiResponse<UserInfo>>('/user/info')
}

// 修改用户信息
export function updateUserInfo(data: Partial<UserInfo>) {
  return request.put<ApiResponse<UserInfo>>('/user/info', data)
}

// 退出登录
export function logout() {
  return request.post<ApiResponse<void>>('/user/logout')
}

// 修改密码
export function updatePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put<ApiResponse<void>>('/user/password', data)
}
