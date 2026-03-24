// 用户信息
export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar: string
  role: number // 0-学员 1-讲师
  status: number
  createdAt: string
}

// 登录参数
export interface LoginParams {
  phone: string
  password: string
}

// 注册参数
export interface RegisterParams {
  phone: string
  password: string
  code: string
  nickname?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}
