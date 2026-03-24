<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderList } from '@/api/order'

const router = useRouter()

interface PurchasedCourse {
  courseId: number
  courseTitle: string
  courseCoverUrl: string
  amount: number
  paidAt: string
}

const loading = ref(true)
const courses = ref<PurchasedCourse[]>([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

async function fetchMyCourses() {
  if (!isLoggedIn.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res: any = await getOrderList({ status: 1, page: 1, pageSize: 50 })
    const data = res.data || res
    const items = data.items || []
    
    const seen = new Set<number>()
    courses.value = items
      .filter((order: any) => {
        if (seen.has(order.courseId)) return false
        seen.add(order.courseId)
        return true
      })
      .map((order: any) => ({
        courseId: order.courseId,
        courseTitle: order.courseTitle || `课程 #${order.courseId}`,
        courseCoverUrl: order.courseCoverUrl || '',
        amount: order.amount,
        paidAt: order.paidAt,
      }))
  } catch (e: any) {
    ElMessage.error(`获取课程列表失败：${e?.message || '未知错误'}`)
  } finally {
    loading.value = false
  }
}

function goToCourse(courseId: number) {
  router.push(`/course/${courseId}`)
}

onMounted(() => {
  fetchMyCourses()
})
</script>

<template>
  <div class="my-courses-page">
    <div class="container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">我的课程</h1>
          <p class="page-desc" v-if="isLoggedIn && !loading">已购 {{ courses.length }} 门课程</p>
        </div>
        <el-button v-if="isLoggedIn" type="primary" round @click="router.push('/courses')">
          继续选课 →
        </el-button>
      </div>

      <!-- 未登录 -->
      <div v-if="!isLoggedIn" class="empty-card">
        <div class="empty-icon">🔒</div>
        <p class="empty-title">请先登录</p>
        <p class="empty-desc">登录后可查看已购课程、继续学习</p>
        <el-button type="primary" size="large" round @click="router.push('/login')">去登录</el-button>
      </div>

      <!-- 加载中 -->
      <div v-else-if="loading" class="loading-card">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 课程列表 -->
      <div v-else-if="courses.length > 0" class="course-grid">
        <div v-for="c in courses" :key="c.courseId" class="course-card" @click="goToCourse(c.courseId)">
          <div class="card-cover">
            <img v-if="c.courseCoverUrl" :src="c.courseCoverUrl" :alt="c.courseTitle" />
            <div v-else class="cover-fallback">📘</div>
            <div class="card-badge">已购</div>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ c.courseTitle }}</h3>
            <div class="card-meta">
              <span class="price-tag">¥{{ c.amount }}</span>
              <span class="date-tag" v-if="c.paidAt">{{ new Date(c.paidAt).toLocaleDateString('zh-CN') }} 购入</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="primary" size="small" round @click.stop="goToCourse(c.courseId)">
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-card">
        <div class="empty-icon">🎒</div>
        <p class="empty-title">还没有已购课程</p>
        <p class="empty-desc">去课程中心挑选一门好课开始学习吧！</p>
        <el-button type="primary" size="large" round @click="router.push('/courses')">去选课</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-courses-page {
  padding: 32px 0 60px;
  min-height: calc(100vh - 200px);
  background: linear-gradient(180deg, #f0f4ff 0%, #fafbff 100%);
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.page-desc {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

/* 课程网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

/* 课程卡片 */
.course-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
  border: 1px solid #f1f5f9;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
  border-color: #e2e8f0;
}

.card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.course-card:hover .card-cover img {
  transform: scale(1.06);
}

.cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
}

.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 20px;
  letter-spacing: 1px;
}

.card-body {
  padding: 16px 16px 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-tag {
  font-size: 15px;
  font-weight: 700;
  color: #4f46e5;
}

.date-tag {
  font-size: 12px;
  color: #94a3b8;
}

.card-footer {
  padding: 8px 16px 16px;
  display: flex;
  justify-content: flex-end;
}

/* 空状态 / 加载状态 */
.empty-card,
.loading-card {
  text-align: center;
  background: #fff;
  border-radius: 20px;
  padding: 80px 40px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.05);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 24px;
}

.loading-card p {
  color: #94a3b8;
  margin-top: 16px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e2e8f0;
  border-top-color: #4f46e5;
  border-radius: 50%;
  margin: 0 auto;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
