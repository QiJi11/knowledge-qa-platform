import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout } from '@/api/user'
import type { UserInfo, LoginParams, RegisterParams } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isTeacher = computed(() => userInfo.value?.role === 1)

  // 登录
  async function loginAction(params: LoginParams) {
    const res = await login(params)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
    return res
  }

  // 注册
  async function registerAction(params: RegisterParams) {
    const res = await register(params)
    return res
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    const res = await getUserInfo()
    userInfo.value = res.data
  }

  // 退出登录
  async function logoutAction() {
    try {
      await logout()
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
    }
  }

  // 初始化时获取用户信息
  if (token.value) {
    fetchUserInfo()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isTeacher,
    loginAction,
    registerAction,
    fetchUserInfo,
    logoutAction
  }
})
