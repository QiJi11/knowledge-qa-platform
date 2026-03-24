<template>
  <div class="course-list-page">
    <div class="container">
      <h1 class="page-title">全部课程</h1>
      
      <!-- 筛选器 -->
      <div class="filters">
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索课程..." 
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <select v-model="sortBy" @change="handleSearch" class="sort-select">
          <option value="viewCount">按热度</option>
          <option value="buyCount">按购买量</option>
          <option value="publishedAt">按发布时间</option>
          <option value="createdAt">按创建时间</option>
        </select>
      </div>

      <!-- 课程网格 -->
      <div v-if="loading" class="courses-grid">
        <div v-for="i in 12" :key="i" class="skeleton-card">
          <div class="skeleton" style="height: 180px;"></div>
          <div style="padding: 16px;">
            <div class="skeleton" style="height: 20px; margin-bottom: 10px;"></div>
            <div class="skeleton" style="height: 16px; width: 80%;"></div>
          </div>
        </div>
      </div>

      <div v-else-if="courses.length > 0" class="courses-grid">
        <CourseCard v-for="course in courses" :key="course.id" :course="course" />
      </div>

      <div v-else class="empty-state">
        <p>未找到相关课程</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCourseList, type Course } from '@/api/course'
import CourseCard from '@/components/course/CourseCard.vue'

const route = useRoute()

const loading = ref(true)
const courses = ref<Course[]>([])
const searchQuery = ref((route.query.q as string) || '')
const sortBy = ref<'publishedAt' | 'createdAt' | 'viewCount' | 'buyCount'>('viewCount')

const handleSearch = async () => {
  try {
    loading.value = true
    const res = await getCourseList({
      keyword: searchQuery.value || undefined,
      sortBy: sortBy.value,
      order: 'desc',
      page: 1,
      pageSize: 12
    })
    courses.value = res.items
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听路由查询参数变化
watch(() => route.query.q, (newQuery) => {
  searchQuery.value = (newQuery as string) || ''
  handleSearch()
})

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.course-list-page {
  padding: 40px 0;
  min-height: 60vh;
}

.page-title {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 30px;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 30px;
}

.search-input,
.sort-select {
  padding: 10px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-size: 14px;
}

.search-input {
  flex: 1;
  max-width: 400px;
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
</style>
