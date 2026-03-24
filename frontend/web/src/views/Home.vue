<template>
  <div class="home">
    <!-- Banner 轮播 -->
    <div class="banner">
      <div 
        class="banner-slide"
      >
        <img :src="banners[currentBanner]?.image" :alt="banners[currentBanner]?.title" fetchpriority="high" decoding="async" class="banner-bg" />
        <div class="banner-overlay"></div>
        <div class="container">
          <div class="banner-content">
            <h2>{{ banners[currentBanner]?.title }}</h2>
            <p>{{ banners[currentBanner]?.subtitle }}</p>
          </div>
        </div>
      </div>
      <div class="banner-indicators">
        <span 
          v-for="(_, index) in banners" 
          :key="index"
          :class="{ active: currentBanner === index }"
          @click="currentBanner = index"
        ></span>
      </div>
    </div>

    <!-- 课程分类 -->
    <div class="categories">
      <div class="container">
        <div class="category-list">
          <div 
            v-for="cat in categories" 
            :key="cat.name"
            class="category-item"
            :class="{ active: selectedCategory === cat.name }"
            @click="selectedCategory = cat.name"
          >
            {{ cat.name }}
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐课程 -->
    <div class="courses-section">
      <div class="container">
        <h2 class="section-title">热门课程推荐</h2>
        
        <!-- 加载中 -->
        <div v-if="loading" class="courses-grid">
          <div v-for="i in 8" :key="i" class="skeleton-card">
            <div class="skeleton" style="height: 180px;"></div>
            <div style="padding: 16px;">
              <div class="skeleton" style="height: 20px; margin-bottom: 10px;"></div>
              <div class="skeleton" style="height: 16px; width: 80%;"></div>
            </div>
          </div>
        </div>

        <!-- 课程列表 -->
        <div v-else-if="courses.length > 0" class="courses-grid">
          <CourseCard v-for="course in courses" :key="course.id" :course="course" />
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-state">
          <p>暂无课程，敬请期待...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { getCourseList, type Course } from '@/api/course'
import CourseCard from '@/components/course/CourseCard.vue'

// Banner 数据
const banners = [
  { title: 'Spring Boot + Vue3 全栈开发', subtitle: '从零开始构建企业级应用', image: '/covers/cover_backend.png' },
  { title: '大模型与 AI 深度应用', subtitle: '把握人工智能时代的新红利', image: '/covers/cover_data.png' },
  { title: 'Vue 3 组合式 API 实战', subtitle: '深入掌握现代前端开发', image: '/covers/cover_frontend.png' },
]
const currentBanner = ref(0)

// 自动轮播
let bannerInterval: number | null = null
onMounted(() => {
  bannerInterval = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % banners.length
  }, 4000)
})

// 清理定时器
onUnmounted(() => {
  if (bannerInterval) {
    clearInterval(bannerInterval)
  }
})

// 分类数据
const categories = [
  { name: '全部' },
  { name: 'Java' },
  { name: 'Spring Boot' },
  { name: '数据库' },
  { name: 'Redis' },
  { name: 'AI 问答' },
  { name: '系统架构' },
  { name: '面试题库' },
]
const selectedCategory = ref('全部')

// 课程数据
const loading = ref(true)
const courses = ref<Course[]>([])

// 获取课程列表
const fetchCourses = async () => {
  try {
    loading.value = true
    const res = await getCourseList({ 
      page: 1, 
      pageSize: 12, 
      category: selectedCategory.value === '全部' ? undefined : selectedCategory.value,
      sortBy: 'publishedAt', 
      order: 'desc' 
    })
    courses.value = res.items
  } catch (error) {
    console.error('获取课程失败:', error)
  } finally {
    loading.value = false
  }
}

watch(selectedCategory, () => {
  fetchCourses()
})

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.home {
  min-height: 100vh;
}

/* Banner 样式 */
.banner {
  position: relative;
  height: 400px;
  overflow: hidden;
  border-radius: 0 0 20px 20px;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
}

.banner-slide {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.banner-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  will-change: transform;
}

.banner-content {
  position: relative;
  z-index: 2;
  color: white;
}

.banner-content h2 {
  font-size: 42px;
  margin-bottom: 16px;
  font-weight: 700;
}

.banner-content p {
  font-size: 20px;
  opacity: 0.9;
}

.banner-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
}

.banner-indicators span {
  width: 30px;
  height: 4px;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s;
}

.banner-indicators span.active {
  background: white;
}

/* 分类导航 */
.categories {
  background: white;
  padding: 28px 0;
  border-bottom: 1px solid var(--border-color);
}

.category-list {
  display: flex;
  gap: 24px;
  flex-wrap: nowrap;
  justify-content: center;
}

.category-item {
  padding: 10px 24px;
  cursor: pointer;
  border-radius: 20px;
  transition: all 0.3s;
  font-size: 16px;
  font-weight: 500;
  color: var(--text-secondary);
}

.category-item:hover,
.category-item.active {
  background: var(--primary-color);
  color: white;
}

/* 课程区域 */
.courses-section {
  padding: 50px 0 60px;
}

.section-title {
  font-size: 32px;
  font-weight: 800;
  margin-bottom: 40px;
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.section-title::after {
  content: '';
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-color), #8b5cf6);
  border-radius: 4px;
}

.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.skeleton-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  color: var(--text-tertiary);
  font-size: 16px;
}

@media (max-width: 768px) {
  .banner {
    height: 250px;
  }
  
  .banner-content h2 {
    font-size: 28px;
  }
  
  .banner-content p {
    font-size: 16px;
  }
  
  .courses-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
}
</style>
