<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Course, Teacher } from '@/types/course'

const route = useRoute()
const router = useRouter()

const teacherId = computed(() => Number(route.params.id))

// 模拟讲师数据
const teacher = ref<Teacher>({
  id: 1,
  nickname: '张老师',
  avatar: 'https://picsum.photos/200/200?random=60',
  intro: '10年Java开发经验，前阿里P7工程师，专注于分布式系统架构设计。擅长Spring Cloud、Redis、消息队列等技术，有丰富的高并发系统设计经验。',
  courseCount: 8,
  studentCount: 15600
})

// 模拟讲师课程
const courses = ref<Course[]>([
  {
    id: 1,
    title: 'Spring Cloud Alibaba 微服务架构实战',
    description: '从零开始搭建企业级微服务架构',
    coverUrl: 'https://picsum.photos/400/225?random=61',
    teacherId: 1,
    categoryId: 2,
    price: 299,
    originalPrice: 399,
    status: 1,
    buyCount: 1580,
    viewCount: 28900,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 4,
    title: 'RocketMQ 消息中间件实战',
    description: '掌握分布式消息队列核心技术',
    coverUrl: 'https://picsum.photos/400/225?random=62',
    teacherId: 1,
    categoryId: 2,
    price: 188,
    originalPrice: 288,
    status: 1,
    buyCount: 670,
    viewCount: 12300,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  }
])

function goToCourse(id: number) {
  router.push(`/course/${id}`)
}

function formatCount(count: number): string {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count.toString()
}
</script>

<template>
  <div class="teacher-detail-page">
    <div class="container">
      <!-- 讲师信息卡片 -->
      <div class="teacher-card">
        <el-avatar :size="120" :src="teacher.avatar">
          {{ teacher.nickname?.charAt(0) }}
        </el-avatar>
        <div class="teacher-info">
          <h1>{{ teacher.nickname }}</h1>
          <p class="intro">{{ teacher.intro }}</p>
          <div class="stats">
            <div class="stat-item">
              <span class="value">{{ teacher.courseCount }}</span>
              <span class="label">课程</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ formatCount(teacher.studentCount || 0) }}</span>
              <span class="label">学员</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 讲师课程 -->
      <div class="courses-section">
        <h2>TA 的课程</h2>
        <div class="course-grid">
          <div
            v-for="course in courses"
            :key="course.id"
            class="course-card card-hover"
            @click="goToCourse(course.id)"
          >
            <div class="course-cover">
              <img :src="course.coverUrl" :alt="course.title" />
            </div>
            <div class="course-info">
              <h3>{{ course.title }}</h3>
              <p>{{ course.description }}</p>
              <div class="course-footer">
                <span class="price">¥{{ course.price }}</span>
                <span class="buy-count">{{ formatCount(course.buyCount) }}人学习</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.teacher-detail-page {
  padding: 24px 0;
}

/* 讲师卡片 */
.teacher-card {
  display: flex;
  gap: 32px;
  padding: 40px;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-radius: 16px;
  color: #fff;
  margin-bottom: 32px;
}

.teacher-info {
  flex: 1;
}

.teacher-info h1 {
  font-size: 28px;
  margin-bottom: 16px;
}

.intro {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.8;
  margin-bottom: 24px;
}

.stats {
  display: flex;
  gap: 48px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-item .value {
  font-size: 28px;
  font-weight: 600;
}

.stat-item .label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 4px;
}

/* 课程列表 */
.courses-section h2 {
  font-size: 20px;
  margin-bottom: 24px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.course-cover {
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.course-card:hover .course-cover img {
  transform: scale(1.05);
}

.course-info {
  padding: 16px;
}

.course-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-info p {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

.buy-count {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .teacher-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .stats {
    justify-content: center;
  }

  .course-grid {
    grid-template-columns: 1fr;
  }
}
</style>
