<script setup lang="ts">
import { ref, computed } from 'vue'
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search, User, ShoppingCart } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const categories = ref([
  { id: 1, name: '前端开发' },
  { id: 2, name: '后端开发' },
  { id: 3, name: '移动开发' },
  { id: 4, name: '人工智能' },
  { id: 5, name: '数据库' },
  { id: 6, name: '云计算' }
])

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'CourseList', query: { keyword: searchKeyword.value } })
  }
}

function handleLogout() {
  userStore.logoutAction()
  router.push('/')
}
</script>

<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content container">
        <!-- Logo -->
        <RouterLink to="/" class="logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">EduPlatform</span>
        </RouterLink>

        <!-- 分类导航 -->
        <nav class="nav-menu">
          <RouterLink to="/courses" class="nav-item">全部课程</RouterLink>
          <RouterLink to="/todos" class="nav-item">待办联调</RouterLink>
          <el-dropdown v-for="cat in categories" :key="cat.id" trigger="hover">
            <span class="nav-item">
              {{ cat.name }}
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push({ name: 'CourseList', query: { categoryId: cat.id } })">
                  查看全部
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程"
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
            clearable
          />
        </div>

        <!-- 用户区域 -->
        <div class="user-area">
          <template v-if="isLoggedIn">
            <RouterLink to="/user/courses" class="user-link">
              <el-icon><Reading /></el-icon>
              <span>我的学习</span>
            </RouterLink>
            <el-dropdown trigger="click">
              <div class="user-avatar">
                <el-avatar :size="32" :src="userInfo?.avatar">
                  {{ userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/user')">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push('/user/courses')">
                    <el-icon><Reading /></el-icon>
                    我的课程
                  </el-dropdown-item>
                  <el-dropdown-item @click="router.push('/user/orders')">
                    <el-icon><Document /></el-icon>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <RouterLink to="/login" class="auth-link">登录</RouterLink>
            <RouterLink to="/register" class="auth-link primary">注册</RouterLink>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <RouterView />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>关于我们</h4>
            <p>EduPlatform 是一个专注于技术教育的在线学习平台，提供高质量的编程课程。</p>
          </div>
          <div class="footer-section">
            <h4>快速链接</h4>
            <RouterLink to="/courses">全部课程</RouterLink>
            <RouterLink to="/user">个人中心</RouterLink>
          </div>
          <div class="footer-section">
            <h4>联系我们</h4>
            <p>邮箱：support@eduplatform.com</p>
            <p>电话：400-123-4567</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2024 EduPlatform. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* Header */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.header-content {
  display: flex;
  align-items: center;
  height: 64px;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
}

.logo-icon {
  font-size: 28px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  padding: 8px 12px;
  color: var(--text-color);
  cursor: pointer;
  transition: color 0.3s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-item:hover {
  color: var(--primary-color);
}

.search-box {
  flex: 1;
  max-width: 360px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 20px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-color);
  font-size: 14px;
}

.user-link:hover {
  color: var(--primary-color);
}

.user-avatar {
  cursor: pointer;
}

.auth-link {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  color: var(--text-color);
}

.auth-link:hover {
  color: var(--primary-color);
}

.auth-link.primary {
  background: var(--primary-color);
  color: #fff;
}

.auth-link.primary:hover {
  background: #2563eb;
  color: #fff;
}

/* Main */
.main-content {
  flex: 1;
  padding: 24px 0;
}

/* Footer */
.footer {
  background: #1f2937;
  color: #9ca3af;
  padding: 48px 0 24px;
  margin-top: auto;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 48px;
  margin-bottom: 32px;
}

.footer-section h4 {
  color: #fff;
  margin-bottom: 16px;
  font-size: 16px;
}

.footer-section p,
.footer-section a {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #9ca3af;
}

.footer-section a:hover {
  color: #fff;
}

.footer-bottom {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #374151;
  font-size: 14px;
}
</style>
