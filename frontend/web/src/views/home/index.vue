<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { Course, Category } from '@/types/course'
import {
  Monitor,
  Setting,
  Iphone,
  Cpu,
  Coin,
  Cloudy,
  DataAnalysis,
  Lock,
  ArrowRight,
  User,
  Avatar,
  Collection,
  Briefcase,
  Trophy
} from '@element-plus/icons-vue'

const router = useRouter()

// 轮播图数据
const banners = ref([
  {
    id: 1,
    title: 'Spring Cloud 微服务架构实战',
    subtitle: '大厂架构师带你从0到1搭建企业级微服务',
    tag: '限时特惠',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    link: '/course/1'
  },
  {
    id: 2,
    title: 'Vue3 + TypeScript 企业级项目',
    subtitle: '完整项目实战，快速上手前端工程化',
    tag: '热门推荐',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    link: '/course/2'
  },
  {
    id: 3,
    title: 'Redis 从入门到精通',
    subtitle: '掌握高性能缓存技术，应对百万级并发',
    tag: '新课上架',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    link: '/course/3'
  }
])

// 分类数据 - 使用Element Plus图标组件名
const categoryIcons: Record<number, any> = {
  1: Monitor,
  2: Setting,
  3: Iphone,
  4: Cpu,
  5: Coin,
  6: Cloudy,
  7: DataAnalysis,
  8: Lock
}

const categories = ref<Category[]>([
  { id: 1, name: '前端开发', icon: 'Monitor', parentId: 0, sortOrder: 1 },
  { id: 2, name: '后端开发', icon: 'Setting', parentId: 0, sortOrder: 2 },
  { id: 3, name: '移动开发', icon: 'Iphone', parentId: 0, sortOrder: 3 },
  { id: 4, name: '人工智能', icon: 'Cpu', parentId: 0, sortOrder: 4 },
  { id: 5, name: '数据库', icon: 'Coin', parentId: 0, sortOrder: 5 },
  { id: 6, name: '云计算', icon: 'Cloudy', parentId: 0, sortOrder: 6 },
  { id: 7, name: '大数据', icon: 'DataAnalysis', parentId: 0, sortOrder: 7 },
  { id: 8, name: '网络安全', icon: 'Lock', parentId: 0, sortOrder: 8 }
])

// 热门课程
const hotCourses = ref<Course[]>([
  {
    id: 1,
    title: 'Spring Cloud Alibaba 微服务架构实战',
    description: '从零开始搭建企业级微服务架构',
    coverUrl: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&h=225&fit=crop',
    teacherId: 1,
    teacher: { id: 1, nickname: '张老师', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50&h=50&fit=crop' },
    categoryId: 2,
    price: 299,
    originalPrice: 399,
    status: 1,
    buyCount: 15800,
    viewCount: 289000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 2,
    title: 'Vue3 + TypeScript 从入门到企业级实战',
    description: '最新 Vue3 Composition API 完全指南',
    coverUrl: 'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&h=225&fit=crop',
    teacherId: 2,
    teacher: { id: 2, nickname: '李老师', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50&h=50&fit=crop' },
    categoryId: 1,
    price: 199,
    originalPrice: 299,
    status: 1,
    buyCount: 23500,
    viewCount: 456000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 3,
    title: 'Redis 核心技术与实战',
    description: '深入理解 Redis 原理，掌握高性能缓存方案',
    coverUrl: 'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400&h=225&fit=crop',
    teacherId: 3,
    teacher: { id: 3, nickname: '王老师', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=50&h=50&fit=crop' },
    categoryId: 5,
    price: 168,
    originalPrice: 268,
    status: 1,
    buyCount: 8900,
    viewCount: 156000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 4,
    title: 'RocketMQ 消息中间件实战',
    description: '掌握分布式消息队列核心技术',
    coverUrl: 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=400&h=225&fit=crop',
    teacherId: 1,
    teacher: { id: 1, nickname: '张老师', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=50&h=50&fit=crop' },
    categoryId: 2,
    price: 188,
    originalPrice: 288,
    status: 1,
    buyCount: 6700,
    viewCount: 123000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  }
])

// 新上好课
const newCourses = ref<Course[]>([
  {
    id: 5,
    title: 'MySQL 性能优化与高可用方案',
    description: '数据库调优必备技能',
    coverUrl: 'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=400&h=225&fit=crop',
    teacherId: 4,
    teacher: { id: 4, nickname: '赵老师', avatar: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?w=50&h=50&fit=crop' },
    categoryId: 5,
    price: 158,
    originalPrice: 258,
    status: 1,
    buyCount: 4560,
    viewCount: 89000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 6,
    title: 'Docker + Kubernetes 云原生实战',
    description: '容器化部署与编排完全指南',
    coverUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400&h=225&fit=crop',
    teacherId: 5,
    teacher: { id: 5, nickname: '钱老师', avatar: 'https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=50&h=50&fit=crop' },
    categoryId: 6,
    price: 268,
    originalPrice: 368,
    status: 1,
    buyCount: 3200,
    viewCount: 67000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 7,
    title: 'React 18 新特性与最佳实践',
    description: '掌握 React 最新技术栈',
    coverUrl: 'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=400&h=225&fit=crop',
    teacherId: 2,
    teacher: { id: 2, nickname: '李老师', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=50&h=50&fit=crop' },
    categoryId: 1,
    price: 178,
    originalPrice: 278,
    status: 1,
    buyCount: 5890,
    viewCount: 112000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 8,
    title: 'JVM 调优实战指南',
    description: '深入 JVM 内存模型与 GC 调优',
    coverUrl: 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=400&h=225&fit=crop',
    teacherId: 3,
    teacher: { id: 3, nickname: '王老师', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=50&h=50&fit=crop' },
    categoryId: 2,
    price: 198,
    originalPrice: 298,
    status: 1,
    buyCount: 4120,
    viewCount: 78000,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  }
])

// 学习路径 - 使用图标组件
const learningPathIcons: Record<number, any> = {
  1: Setting,    // Java后端
  2: Monitor,    // 前端架构师
  3: DataAnalysis, // 大数据
  4: Cloudy      // DevOps
}

const learningPaths = ref([
  { id: 1, title: 'Java后端工程师', courses: 12, students: 8600, color: '#f56c6c' },
  { id: 2, title: '前端架构师', courses: 10, students: 7200, color: '#409eff' },
  { id: 3, title: '大数据工程师', courses: 8, students: 5400, color: '#67c23a' },
  { id: 4, title: 'DevOps工程师', courses: 9, students: 4800, color: '#e6a23c' }
])

function goToCourse(id: number) {
  router.push(`/course/${id}`)
}

function goToCategory(id: number) {
  router.push({ name: 'CourseList', query: { categoryId: id } })
}

function formatCount(count: number): string {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count.toString()
}
</script>

<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="container">
        <div class="hero-wrapper">
          <div class="category-menu">
            <div class="menu-title">课程分类</div>
            <div
              v-for="cat in categories"
              :key="cat.id"
              class="menu-item"
              @click="goToCategory(cat.id)"
            >
              <el-icon class="menu-icon" :size="20"><component :is="categoryIcons[cat.id]" /></el-icon>
              <span class="menu-name">{{ cat.name }}</span>
              <el-icon class="menu-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
          <div class="hero-carousel">
            <el-carousel height="400px" :interval="5000">
              <el-carousel-item v-for="banner in banners" :key="banner.id">
                <div
                  class="banner-item"
                  :style="{ background: banner.gradient }"
                  @click="router.push(banner.link)"
                >
                  <div class="banner-content">
                    <span class="banner-tag">{{ banner.tag }}</span>
                    <h2>{{ banner.title }}</h2>
                    <p>{{ banner.subtitle }}</p>
                    <el-button type="primary" size="large" round>立即学习</el-button>
                  </div>
                  <div class="banner-decoration">
                    <div class="circle c1"></div>
                    <div class="circle c2"></div>
                    <div class="circle c3"></div>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
        </div>
      </div>
    </section>

    <section class="stats-section">
      <div class="container">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-value">200<span>+</span></div>
            <div class="stat-label">精品课程</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">50<span>+</span></div>
            <div class="stat-label">资深讲师</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">100<span>w+</span></div>
            <div class="stat-label">学员数量</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">98<span>%</span></div>
            <div class="stat-label">好评率</div>
          </div>
        </div>
      </div>
    </section>

    <section class="course-section">
      <div class="container">
        <div class="section-header">
          <div class="section-title-wrapper">
            <h2 class="section-title">热门推荐</h2>
            <span class="section-subtitle">学员最喜爱的课程</span>
          </div>
          <router-link to="/courses?sort=hottest" class="more-link">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="course-grid">
          <div
            v-for="course in hotCourses"
            :key="course.id"
            class="course-card"
            @click="goToCourse(course.id)"
          >
            <div class="course-cover">
              <img :src="course.coverUrl" :alt="course.title" />
              <div class="course-overlay">
                <el-button type="primary" round>立即学习</el-button>
              </div>
              <div class="course-tag hot">HOT</div>
            </div>
            <div class="course-info">
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-desc">{{ course.description }}</p>
              <div class="course-teacher">
                <img :src="course.teacher?.avatar" class="teacher-avatar" />
                <span class="teacher-name">{{ course.teacher?.nickname }}</span>
                <span class="course-students">
                  <el-icon><User /></el-icon>
                  {{ formatCount(course.buyCount) }}人学习
                </span>
              </div>
              <div class="course-footer">
                <div class="price-info">
                  <span class="price">¥{{ course.price }}</span>
                  <span class="original-price" v-if="course.originalPrice">¥{{ course.originalPrice }}</span>
                </div>
                <div class="discount-tag" v-if="course.originalPrice">
                  {{ Math.round((1 - course.price / course.originalPrice) * 100) }}% OFF
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="path-section">
      <div class="container">
        <div class="section-header">
          <div class="section-title-wrapper">
            <h2 class="section-title">学习路径</h2>
            <span class="section-subtitle">体系化学习，快速成长</span>
          </div>
        </div>
        <div class="path-grid">
          <div
            v-for="path in learningPaths"
            :key="path.id"
            class="path-card"
            :style="{ '--path-color': path.color }"
          >
            <div class="path-icon" :style="{ background: path.color }">
              <el-icon :size="28" color="#fff"><component :is="learningPathIcons[path.id]" /></el-icon>
            </div>
            <div class="path-info">
              <h3>{{ path.title }}</h3>
              <p>{{ path.courses }}门课程 · {{ formatCount(path.students) }}人学习</p>
            </div>
            <el-icon class="path-arrow"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <section class="course-section new-course">
      <div class="container">
        <div class="section-header">
          <div class="section-title-wrapper">
            <h2 class="section-title">新上好课</h2>
            <span class="section-subtitle">最新上架的优质课程</span>
          </div>
          <router-link to="/courses?sort=newest" class="more-link">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="course-grid">
          <div
            v-for="course in newCourses"
            :key="course.id"
            class="course-card"
            @click="goToCourse(course.id)"
          >
            <div class="course-cover">
              <img :src="course.coverUrl" :alt="course.title" />
              <div class="course-overlay">
                <el-button type="primary" round>立即学习</el-button>
              </div>
              <div class="course-tag new">NEW</div>
            </div>
            <div class="course-info">
              <h3 class="course-title">{{ course.title }}</h3>
              <p class="course-desc">{{ course.description }}</p>
              <div class="course-teacher">
                <img :src="course.teacher?.avatar" class="teacher-avatar" />
                <span class="teacher-name">{{ course.teacher?.nickname }}</span>
                <span class="course-students">
                  <el-icon><User /></el-icon>
                  {{ formatCount(course.buyCount) }}人学习
                </span>
              </div>
              <div class="course-footer">
                <div class="price-info">
                  <span class="price">¥{{ course.price }}</span>
                  <span class="original-price" v-if="course.originalPrice">¥{{ course.originalPrice }}</span>
                </div>
                <div class="discount-tag" v-if="course.originalPrice">
                  {{ Math.round((1 - course.price / course.originalPrice) * 100) }}% OFF
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="feature-section">
      <div class="container">
        <div class="section-header center">
          <h2 class="section-title">为什么选择我们</h2>
          <span class="section-subtitle">专注于高质量技术教育</span>
        </div>
        <div class="feature-grid">
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon :size="36"><Avatar /></el-icon>
            </div>
            <h3>大厂讲师</h3>
            <p>来自阿里、腾讯、字节等大厂的资深工程师</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon :size="36"><Collection /></el-icon>
            </div>
            <h3>体系化课程</h3>
            <p>从入门到精通，完整的知识体系</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon :size="36"><Briefcase /></el-icon>
            </div>
            <h3>项目实战</h3>
            <p>真实企业级项目，学完即可上手</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon-wrapper">
              <el-icon :size="36"><Trophy /></el-icon>
            </div>
            <h3>终身学习</h3>
            <p>一次购买，永久学习，持续更新</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  background: #f8fafc;
}

.hero-section {
  padding: 24px 0;
  background: linear-gradient(180deg, #f0f5ff 0%, #f8fafc 100%);
}

.hero-wrapper {
  display: flex;
  gap: 16px;
  height: 400px;
}

.category-menu {
  width: 220px;
  background: #fff;
  border-radius: 12px;
  padding: 8px 0;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.menu-title {
  padding: 12px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  border-bottom: 1px solid #f0f0f0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.menu-item:hover {
  background: #f0f5ff;
  color: #3b82f6;
}

.menu-icon {
  margin-right: 12px;
  color: #6b7280;
}

.menu-item:hover .menu-icon {
  color: #3b82f6;
}

.menu-name {
  flex: 1;
  font-size: 14px;
}

.menu-arrow {
  color: #9ca3af;
  font-size: 12px;
}

.hero-carousel {
  flex: 1;
  border-radius: 12px;
  overflow: hidden;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 60px;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.banner-content {
  position: relative;
  z-index: 2;
  color: #fff;
}

.banner-tag {
  display: inline-block;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 12px;
  margin-bottom: 16px;
}

.banner-content h2 {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
}

.banner-content p {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 24px;
}

.banner-decoration {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 50%;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.c1 { width: 300px; height: 300px; right: -50px; top: -50px; }
.c2 { width: 200px; height: 200px; right: 100px; bottom: 20px; }
.c3 { width: 150px; height: 150px; right: 50px; top: 50%; }

.stats-section {
  padding: 32px 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  color: #3b82f6;
  margin-bottom: 8px;
}

.stat-value span {
  font-size: 20px;
  font-weight: 500;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 28px;
}

.section-header.center {
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.section-title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.section-subtitle {
  font-size: 14px;
  color: #9ca3af;
}

.more-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
  transition: color 0.2s;
}

.more-link:hover {
  color: #3b82f6;
}

.course-section {
  padding: 48px 0;
}

.course-section.new-course {
  background: #fff;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.course-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.course-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
}

.course-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.course-card:hover .course-cover img {
  transform: scale(1.1);
}

.course-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.course-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.course-tag.hot {
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
}

.course-tag.new {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.course-info {
  padding: 16px;
}

.course-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  height: 45px;
}

.course-desc {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-teacher {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.teacher-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.teacher-name {
  font-size: 13px;
  color: #6b7280;
}

.course-students {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
}

.original-price {
  font-size: 13px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.discount-tag {
  padding: 2px 8px;
  background: #fff1f0;
  color: #f56c6c;
  font-size: 12px;
  font-weight: 500;
  border-radius: 4px;
}

.path-section {
  padding: 48px 0;
  background: #fff;
}

.path-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.path-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e2e8f0;
}

.path-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: var(--path-color);
}

.path-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.path-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.path-info p {
  font-size: 13px;
  color: #9ca3af;
}

.path-arrow {
  margin-left: auto;
  color: #9ca3af;
}

.feature-section {
  padding: 64px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.feature-section .section-title,
.feature-section .section-subtitle {
  color: #fff;
}

.feature-section .section-subtitle {
  opacity: 0.8;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  margin-top: 48px;
}

.feature-item {
  text-align: center;
  color: #fff;
}

.feature-icon-wrapper {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.feature-item h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.feature-item p {
  font-size: 14px;
  opacity: 0.8;
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .course-grid, .path-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .hero-wrapper {
    flex-direction: column;
    height: auto;
  }
  .category-menu {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    padding: 12px;
  }
  .menu-title {
    display: none;
  }
  .menu-item {
    padding: 8px 16px;
  }
}

@media (max-width: 992px) {
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stats-grid, .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .path-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .course-grid, .path-grid {
    grid-template-columns: 1fr;
  }
  .stats-grid, .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
