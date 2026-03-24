<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CourseDetail, Chapter, Lesson } from '@/types/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const courseId = computed(() => Number(route.params.id))
const loading = ref(false)
const activeTab = ref('intro')

// 模拟课程详情数据
const course = ref<CourseDetail>({
  id: 1,
  title: 'Spring Cloud Alibaba 微服务架构实战',
  description: '从零开始搭建企业级微服务架构，涵盖Nacos、Gateway、Sentinel、RocketMQ等核心组件的实战应用',
  coverUrl: 'https://picsum.photos/800/450?random=30',
  teacherId: 1,
  teacher: {
    id: 1,
    nickname: '张老师',
    avatar: 'https://picsum.photos/100/100?random=1',
    intro: '10年Java开发经验，前阿里P7工程师，专注于分布式系统架构设计',
    courseCount: 8,
    studentCount: 15600
  },
  categoryId: 2,
  category: { id: 2, name: '后端开发', parentId: 0, sortOrder: 2 },
  price: 299,
  originalPrice: 399,
  status: 1,
  buyCount: 1580,
  viewCount: 28900,
  chapterCount: 12,
  lessonCount: 86,
  totalDuration: 36000, // 10小时
  createdAt: '2024-01-01',
  updatedAt: '2024-01-15',
  isBought: false,
  chapters: [
    {
      id: 1,
      courseId: 1,
      title: '第一章：课程介绍与环境搭建',
      sortOrder: 1,
      lessons: [
        { id: 1, chapterId: 1, courseId: 1, title: '1.1 课程介绍与学习路线', duration: 600, isFree: true, sortOrder: 1 },
        { id: 2, chapterId: 1, courseId: 1, title: '1.2 开发环境准备', duration: 900, isFree: true, sortOrder: 2 },
        { id: 3, chapterId: 1, courseId: 1, title: '1.3 创建父工程', duration: 720, isFree: false, sortOrder: 3 }
      ]
    },
    {
      id: 2,
      courseId: 1,
      title: '第二章：Nacos 服务注册与配置中心',
      sortOrder: 2,
      lessons: [
        { id: 4, chapterId: 2, courseId: 1, title: '2.1 Nacos 介绍与安装', duration: 800, isFree: false, sortOrder: 1 },
        { id: 5, chapterId: 2, courseId: 1, title: '2.2 服务注册与发现', duration: 1200, isFree: false, sortOrder: 2 },
        { id: 6, chapterId: 2, courseId: 1, title: '2.3 配置中心实战', duration: 1100, isFree: false, sortOrder: 3 },
        { id: 7, chapterId: 2, courseId: 1, title: '2.4 Nacos 集群部署', duration: 900, isFree: false, sortOrder: 4 }
      ]
    },
    {
      id: 3,
      courseId: 1,
      title: '第三章：Gateway 网关',
      sortOrder: 3,
      lessons: [
        { id: 8, chapterId: 3, courseId: 1, title: '3.1 Gateway 核心概念', duration: 700, isFree: false, sortOrder: 1 },
        { id: 9, chapterId: 3, courseId: 1, title: '3.2 路由配置与过滤器', duration: 1300, isFree: false, sortOrder: 2 },
        { id: 10, chapterId: 3, courseId: 1, title: '3.3 统一鉴权实现', duration: 1500, isFree: false, sortOrder: 3 }
      ]
    },
    {
      id: 4,
      courseId: 1,
      title: '第四章：Sentinel 限流与熔断',
      sortOrder: 4,
      lessons: [
        { id: 11, chapterId: 4, courseId: 1, title: '4.1 Sentinel 介绍', duration: 600, isFree: false, sortOrder: 1 },
        { id: 12, chapterId: 4, courseId: 1, title: '4.2 流量控制规则', duration: 1000, isFree: false, sortOrder: 2 },
        { id: 13, chapterId: 4, courseId: 1, title: '4.3 熔断降级实战', duration: 1200, isFree: false, sortOrder: 3 }
      ]
    }
  ]
})

// 课程亮点
const highlights = [
  '深入理解微服务架构设计原理',
  '掌握 Spring Cloud Alibaba 全家桶',
  '实战企业级项目开发流程',
  '学会分布式事务解决方案',
  '掌握高并发场景下的系统设计'
]

// 课程详情 HTML
const detailHtml = ref(`
  <h3>课程简介</h3>
  <p>本课程将从零开始，带你深入学习 Spring Cloud Alibaba 微服务架构的核心技术，包括服务注册与发现、配置中心、网关、限流熔断、分布式事务等企业级实战内容。</p>

  <h3>你将学到</h3>
  <ul>
    <li>Spring Cloud Alibaba 核心组件的原理与实战</li>
    <li>Nacos 服务注册与配置中心的使用</li>
    <li>Gateway 网关的路由与鉴权</li>
    <li>Sentinel 限流与熔断降级</li>
    <li>RocketMQ 消息队列应用</li>
    <li>分布式事务解决方案</li>
  </ul>

  <h3>适合人群</h3>
  <ul>
    <li>有一定 Java 基础的开发者</li>
    <li>想学习微服务架构的后端工程师</li>
    <li>准备面试需要项目经验的求职者</li>
  </ul>
`)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isBought = computed(() => course.value?.isBought)

// 统计数据
const totalLessons = computed(() => {
  return course.value.chapters.reduce((sum, ch) => sum + (ch.lessons?.length || 0), 0)
})

const totalDurationStr = computed(() => {
  const hours = Math.floor((course.value.totalDuration || 0) / 3600)
  const minutes = Math.floor(((course.value.totalDuration || 0) % 3600) / 60)
  return `${hours}小时${minutes}分钟`
})

// 格式化时长
function formatDuration(seconds: number): string {
  const min = Math.floor(seconds / 60)
  const sec = seconds % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
}

// 播放课时
function playLesson(lesson: Lesson) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  if (!lesson.isFree && !isBought.value) {
    ElMessage.warning('请先购买课程')
    return
  }

  router.push(`/learn/${lesson.id}`)
}

// 购买课程
function buyCourse() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  router.push(`/order/confirm/${course.value.id}`)
}

// 开始学习
function startLearning() {
  const firstLesson = course.value.chapters[0]?.lessons?.[0]
  if (firstLesson) {
    router.push(`/learn/${firstLesson.id}`)
  }
}
</script>

<template>
  <div class="course-detail-page" v-loading="loading">
    <!-- 课程头部 -->
    <section class="course-header">
      <div class="container">
        <div class="header-content">
          <div class="cover-area">
            <img :src="course.coverUrl" :alt="course.title" class="course-cover" />
          </div>
          <div class="info-area">
            <div class="category-tag">{{ course.category?.name }}</div>
            <h1 class="course-title">{{ course.title }}</h1>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-stats">
              <span><el-icon><User /></el-icon> {{ course.buyCount }} 人已购买</span>
              <span><el-icon><View /></el-icon> {{ course.viewCount }} 次浏览</span>
              <span><el-icon><Clock /></el-icon> {{ totalDurationStr }}</span>
              <span><el-icon><Collection /></el-icon> {{ totalLessons }} 课时</span>
            </div>
            <div class="price-area">
              <span class="current-price">¥{{ course.price }}</span>
              <span class="original-price" v-if="course.originalPrice">
                ¥{{ course.originalPrice }}
              </span>
              <el-tag type="danger" size="small" v-if="course.originalPrice">
                省 ¥{{ course.originalPrice - course.price }}
              </el-tag>
            </div>
            <div class="action-area">
              <template v-if="isBought">
                <el-button type="primary" size="large" @click="startLearning">
                  <el-icon><VideoPlay /></el-icon>
                  继续学习
                </el-button>
              </template>
              <template v-else>
                <el-button type="primary" size="large" @click="buyCourse">
                  立即购买
                </el-button>
                <el-button size="large">
                  <el-icon><Star /></el-icon>
                  收藏
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 课程内容 -->
    <section class="course-content">
      <div class="container">
        <div class="content-wrapper">
          <!-- 左侧主内容 -->
          <div class="main-content">
            <el-tabs v-model="activeTab">
              <el-tab-pane label="课程介绍" name="intro">
                <!-- 课程亮点 -->
                <div class="highlights-section">
                  <h3>课程亮点</h3>
                  <div class="highlight-list">
                    <div v-for="(item, index) in highlights" :key="index" class="highlight-item">
                      <el-icon class="check-icon"><CircleCheck /></el-icon>
                      <span>{{ item }}</span>
                    </div>
                  </div>
                </div>
                <!-- 详细介绍 -->
                <div class="detail-section" v-html="detailHtml"></div>
              </el-tab-pane>

              <el-tab-pane label="课程目录" name="catalog">
                <div class="catalog-section">
                  <div class="catalog-header">
                    <span>共 {{ course.chapters.length }} 章 {{ totalLessons }} 课时</span>
                  </div>
                  <el-collapse>
                    <el-collapse-item
                      v-for="chapter in course.chapters"
                      :key="chapter.id"
                      :name="chapter.id"
                    >
                      <template #title>
                        <div class="chapter-title">
                          <span>{{ chapter.title }}</span>
                          <span class="lesson-count">{{ chapter.lessons?.length || 0 }}课时</span>
                        </div>
                      </template>
                      <div class="lesson-list">
                        <div
                          v-for="lesson in chapter.lessons"
                          :key="lesson.id"
                          class="lesson-item"
                          :class="{ free: lesson.isFree }"
                          @click="playLesson(lesson)"
                        >
                          <div class="lesson-info">
                            <el-icon class="play-icon"><VideoPlay /></el-icon>
                            <span class="lesson-title">{{ lesson.title }}</span>
                            <el-tag v-if="lesson.isFree" type="success" size="small">试看</el-tag>
                          </div>
                          <span class="lesson-duration">{{ formatDuration(lesson.duration) }}</span>
                        </div>
                      </div>
                    </el-collapse-item>
                  </el-collapse>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 右侧讲师信息 -->
          <div class="sidebar">
            <div class="teacher-card">
              <h3>讲师介绍</h3>
              <div class="teacher-info">
                <el-avatar :size="64" :src="course.teacher?.avatar">
                  {{ course.teacher?.nickname?.charAt(0) }}
                </el-avatar>
                <div class="teacher-detail">
                  <div class="teacher-name">{{ course.teacher?.nickname }}</div>
                  <div class="teacher-stats">
                    <span>{{ course.teacher?.courseCount }} 门课程</span>
                    <span>{{ course.teacher?.studentCount }} 名学员</span>
                  </div>
                </div>
              </div>
              <p class="teacher-intro">{{ course.teacher?.intro }}</p>
              <el-button
                type="primary"
                plain
                style="width: 100%"
                @click="router.push(`/teacher/${course.teacherId}`)"
              >
                查看讲师主页
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.course-detail-page {
  background: #f5f7fa;
}

/* Header */
.course-header {
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  padding: 48px 0;
  color: #fff;
}

.header-content {
  display: flex;
  gap: 48px;
}

.cover-area {
  flex-shrink: 0;
}

.course-cover {
  width: 480px;
  height: 270px;
  border-radius: 12px;
  object-fit: cover;
}

.info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.category-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 12px;
  width: fit-content;
}

.course-title {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 16px;
  line-height: 1.4;
}

.course-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 20px;
  line-height: 1.6;
}

.course-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 24px;
}

.course-stats span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.price-area {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.current-price {
  font-size: 36px;
  font-weight: 700;
  color: #ff6b6b;
}

.original-price {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.5);
  text-decoration: line-through;
}

.action-area {
  display: flex;
  gap: 16px;
  margin-top: auto;
}

.action-area .el-button {
  padding: 12px 32px;
  font-size: 16px;
}

/* Content */
.course-content {
  padding: 32px 0;
}

.content-wrapper {
  display: flex;
  gap: 24px;
}

.main-content {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

/* Highlights */
.highlights-section {
  margin-bottom: 32px;
}

.highlights-section h3 {
  font-size: 18px;
  margin-bottom: 16px;
}

.highlight-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.highlight-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  font-size: 14px;
}

.check-icon {
  color: var(--success-color);
  font-size: 18px;
}

/* Detail */
.detail-section {
  line-height: 1.8;
}

.detail-section :deep(h3) {
  font-size: 18px;
  margin: 24px 0 12px;
}

.detail-section :deep(ul) {
  padding-left: 20px;
}

.detail-section :deep(li) {
  margin-bottom: 8px;
}

/* Catalog */
.catalog-section {
  margin-top: 16px;
}

.catalog-header {
  margin-bottom: 16px;
  color: var(--text-secondary);
  font-size: 14px;
}

.chapter-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 16px;
}

.lesson-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.lesson-list {
  padding: 0 16px;
}

.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: color 0.3s;
}

.lesson-item:last-child {
  border-bottom: none;
}

.lesson-item:hover {
  color: var(--primary-color);
}

.lesson-item.free:hover {
  color: var(--success-color);
}

.lesson-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.play-icon {
  color: var(--text-secondary);
}

.lesson-title {
  font-size: 14px;
}

.lesson-duration {
  font-size: 12px;
  color: var(--text-secondary);
}

/* Teacher Card */
.teacher-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  position: sticky;
  top: 88px;
}

.teacher-card h3 {
  font-size: 16px;
  margin-bottom: 20px;
}

.teacher-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.teacher-detail {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.teacher-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.teacher-stats {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  gap: 12px;
}

.teacher-intro {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 20px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .header-content {
    flex-direction: column;
    gap: 24px;
  }

  .course-cover {
    width: 100%;
    height: auto;
    aspect-ratio: 16 / 9;
  }

  .content-wrapper {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .teacher-card {
    position: static;
  }
}
</style>
