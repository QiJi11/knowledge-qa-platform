<template>
  <div class="admin-page">
    <div class="container">
      <div class="admin-layout">
        <aside class="admin-sidebar">
          <h2 class="sidebar-title">管理后台</h2>
          <nav class="sidebar-nav">
            <router-link to="/admin" class="sidebar-item" exact-active-class="active">
              <span class="icon">📊</span> 数据概览
            </router-link>
            <router-link to="/admin/courses" class="sidebar-item" active-class="active">
              <span class="icon">📖</span> 课程管理
            </router-link>
            <router-link to="/admin/users" class="sidebar-item" active-class="active">
              <span class="icon">👤</span> 用户管理
            </router-link>
          </nav>
        </aside>

        <main class="admin-main">
          <!-- 数据概览（默认页） -->
          <div v-if="currentView === 'dashboard'" class="dashboard">
            <h1 class="page-header">数据概览</h1>
            <div class="stat-grid">
              <div class="stat-card" v-for="s in statCards" :key="s.label">
                <div class="stat-icon" :style="{ background: s.bg, color: s.color }">{{ s.letter }}</div>
                <div class="stat-info">
                  <span class="stat-value">{{ s.value }}</span>
                  <span class="stat-label">{{ s.label }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 子路由内容 -->
          <router-view v-if="currentView !== 'dashboard'" />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const currentView = computed(() => {
  if (route.path === '/admin') return 'dashboard'
  return 'child'
})

const stats = ref({ userCount: 0, courseCount: 0, taskCount: 0, orderCount: 0 })

const statCards = computed(() => [
  { letter: '人', label: '用户数', value: stats.value.userCount, bg: '#eef2ff', color: '#4f46e5' },
  { letter: '课', label: '课程数', value: stats.value.courseCount, bg: '#ecfdf5', color: '#059669' },
  { letter: '学', label: '学习任务', value: stats.value.taskCount, bg: '#fefce8', color: '#ca8a04' },
  { letter: '单', label: '订单数', value: stats.value.orderCount, bg: '#fff1f2', color: '#e11d48' },
])

onMounted(async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await fetch('/api/v1/admin/stats', {
      headers: { Authorization: `Bearer ${token}` }
    })
    const result = await res.json()
    if (result.code === 0) {
      stats.value = result.data
    }
  } catch (e) {
    // ignore
  }
})
</script>

<style scoped>
.admin-page {
  padding: 24px 0 60px;
  min-height: calc(100vh - 72px);
  background: #f5f7fb;
}

.admin-layout {
  display: flex;
  gap: 24px;
  min-height: 600px;
}

.admin-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 16px;
  padding: 24px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  height: fit-content;
  position: sticky;
  top: 96px;
}

.sidebar-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
  padding-left: 12px;
  color: var(--primary-color, #4361ee);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  color: var(--text-secondary, #666);
  font-size: 15px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s;
}

.sidebar-item:hover {
  background: #f0f3ff;
  color: var(--primary-color, #4361ee);
}

.sidebar-item.active {
  background: linear-gradient(135deg, #4361ee, #6366f1);
  color: #fff;
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.3);
}

.sidebar-item .icon {
  font-size: 18px;
}

.admin-main {
  flex: 1;
  min-width: 0;
}

.page-header {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 20px;
  font-weight: 800;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary, #1f2a37);
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary, #666);
  margin-top: 2px;
}

.dashboard {
  background: transparent;
}
</style>
