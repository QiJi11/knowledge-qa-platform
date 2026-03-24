<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)

const menuItems = [
  { path: '/user', name: 'UserProfile', title: '个人信息', icon: 'User' },
  { path: '/user/courses', name: 'MyCourses', title: '我的课程', icon: 'Reading' },
  { path: '/user/orders', name: 'MyOrders', title: '我的订单', icon: 'Document' }
]

const activeMenu = computed(() => route.path)

function navigateTo(path: string) {
  router.push(path)
}
</script>

<template>
  <div class="user-center">
    <div class="container">
      <div class="user-layout">
        <!-- 左侧菜单 -->
        <aside class="user-sidebar">
          <div class="user-card">
            <el-avatar :size="80" :src="userInfo?.avatar">
              {{ userInfo?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div class="user-name">{{ userInfo?.nickname || '用户' }}</div>
            <div class="user-role">
              <el-tag size="small">
                {{ userInfo?.role === 1 ? '讲师' : '学员' }}
              </el-tag>
            </div>
          </div>

          <el-menu
            :default-active="activeMenu"
            class="user-menu"
          >
            <el-menu-item
              v-for="item in menuItems"
              :key="item.path"
              :index="item.path"
              @click="navigateTo(item.path)"
            >
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-menu>
        </aside>

        <!-- 右侧内容 -->
        <main class="user-content">
          <RouterView />
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-center {
  padding: 24px 0;
  min-height: calc(100vh - 200px);
}

.user-layout {
  display: flex;
  gap: 24px;
}

.user-sidebar {
  width: 240px;
  flex-shrink: 0;
}

.user-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  margin-bottom: 16px;
}

.user-name {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
}

.user-role {
  margin-top: 8px;
}

.user-menu {
  background: #fff;
  border-radius: 12px;
  border: none;
  padding: 8px;
}

.user-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px;
}

.user-menu :deep(.el-menu-item.is-active) {
  background: #f0f5ff;
  color: var(--primary-color);
}

.user-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  min-height: 500px;
}
</style>
