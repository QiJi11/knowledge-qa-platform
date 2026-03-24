<template>
  <header class="header">
    <div class="container">
      <div class="header-content">
        <div class="logo">
          <router-link to="/">
            <h1>学习平台</h1>
          </router-link>
        </div>

        <nav class="nav">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/courses" class="nav-item">课程</router-link>
          <router-link v-if="hasToken" to="/user/courses" class="nav-item">我的课程</router-link>
          <router-link to="/plan" class="nav-item">学习计划</router-link>
          <router-link to="/stats" class="nav-item">学习统计</router-link>
          <router-link to="/chat" class="nav-item ai-link">AI 知识问答</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-item admin-link">管理后台</router-link>
        </nav>

        <div class="header-right">
          <div class="search-box">
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="搜索课程" 
              @keyup.enter="handleSearch"
            />
            <button @click="handleSearch">搜索</button>
          </div>
          
          <div class="auth-links" v-if="!hasToken">
            <router-link to="/login" class="nav-btn">登录</router-link>
            <router-link to="/register" class="nav-btn primary">注册</router-link>
          </div>
          <div class="auth-links" v-else>
            <span class="user-greeting">Hi, {{ nickname }}</span>
            <button @click="logout" class="nav-btn">退出</button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchQuery = ref('')
const hasToken = ref(!!localStorage.getItem('token'))
const nickname = ref(localStorage.getItem('nickname') || '用户')
const isAdmin = ref(localStorage.getItem('role') === 'admin')

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('nickname')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  hasToken.value = false
  isAdmin.value = false
  router.push('/login')
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({ path: '/courses', query: { q: searchQuery.value } })
  }
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  background: white;
  border-bottom: 1px solid var(--border-color);
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  flex-wrap: nowrap;
  gap: 20px;
}

.logo {
  flex-shrink: 0;
  white-space: nowrap;
}

.logo h1 {
  font-size: 20px;
  color: var(--primary-color);
  font-weight: 600;
}

.nav {
  display: flex;
  gap: 28px;
  flex-shrink: 1;
  min-width: 0;
  overflow: hidden;
  margin-left: 24px;
  white-space: nowrap;
}

.nav-item {
  font-size: 14px;
  color: var(--text-primary);
  transition: color 0.3s;
  white-space: nowrap;
}

.nav-item:hover,
.nav-item.router-link-active {
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.search-box {
  display: flex;
  align-items: center;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  overflow: hidden;
}

.search-box input {
  padding: 8px 12px;
  border: none;
  outline: none;
  width: 130px;
}

.search-box button {
  padding: 8px 20px;
  background: var(--primary-color);
  color: white;
  border: none;
  cursor: pointer;
  transition: background 0.3s;
}

.search-box button:hover {
  background: #1557b0;
}

.auth-links {
  display: flex;
  gap: 12px;
  align-items: center;
}

.nav-btn {
  padding: 6px 16px;
  font-size: 14px;
  border-radius: 20px;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  transition: all 0.3s;
}

.nav-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.nav-btn.primary {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

.nav-btn.primary:hover {
  background: #1557b0;
}

.ai-link {
  color: #8b5cf6;
  font-weight: 500;
  display: flex;
  align-items: center;
}
.ai-link::before {
  content: '✨';
  margin-right: 4px;
}
.ai-link:hover {
  color: #7c3aed;
}

.user-greeting {
  font-size: 14px;
  color: var(--text-secondary);
  margin-right: 8px;
}

/* 第一档：≤1024px 缩小间距，隐藏搜索按钮（回车仍可搜） */
@media (max-width: 1024px) {
  .nav { gap: 16px; margin-left: 10px; }
  .search-box button { display: none; }
  .search-box input { width: 100px; }
  .user-greeting { display: none; }
}

/* 第二档：≤768px 隐藏登录/注册按钮 */
@media (max-width: 768px) {
  .nav { gap: 12px; margin-left: 8px; }
  .auth-links { display: none; }
  .logo h1 { font-size: 18px; }
}

/* 第三档：≤600px 隐藏登录注册，保留 Logo + 搜索框，导航自然被挤掉 */
@media (max-width: 600px) {
  .auth-links { display: none; }
  .search-box input { width: 80px; }
  .logo h1 { font-size: 16px; }
}
</style>
