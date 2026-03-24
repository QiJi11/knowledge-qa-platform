<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Course, Category } from '@/types/course'

const route = useRoute()
const router = useRouter()

// 筛选条件
const categoryId = ref<number | undefined>(Number(route.query.categoryId) || undefined)
const keyword = ref(route.query.keyword as string || '')
const sortBy = ref(route.query.sort as string || 'newest')
const priceRange = ref<[number, number]>([0, 1000])

// 分类数据
const categories = ref<Category[]>([
  { id: 0, name: '全部', parentId: 0, sortOrder: 0 },
  { id: 1, name: '前端开发', parentId: 0, sortOrder: 1 },
  { id: 2, name: '后端开发', parentId: 0, sortOrder: 2 },
  { id: 3, name: '移动开发', parentId: 0, sortOrder: 3 },
  { id: 4, name: '人工智能', parentId: 0, sortOrder: 4 },
  { id: 5, name: '数据库', parentId: 0, sortOrder: 5 },
  { id: 6, name: '云计算', parentId: 0, sortOrder: 6 }
])

// 排序选项
const sortOptions = [
  { label: '最新上架', value: 'newest' },
  { label: '最多购买', value: 'hottest' },
  { label: '价格从低到高', value: 'price_asc' },
  { label: '价格从高到低', value: 'price_desc' }
]

// 模拟课程数据
const courses = ref<Course[]>([
  {
    id: 1,
    title: 'Spring Cloud Alibaba 微服务架构实战',
    description: '从零开始搭建企业级微服务架构，涵盖Nacos、Gateway、Sentinel等核心组件',
    coverUrl: 'https://picsum.photos/400/225?random=20',
    teacherId: 1,
    teacher: { id: 1, nickname: '张三', avatar: '' },
    categoryId: 2,
    price: 299,
    originalPrice: 399,
    status: 1,
    buyCount: 1580,
    viewCount: 28900,
    chapterCount: 12,
    lessonCount: 86,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 2,
    title: 'Vue3 + TypeScript 从入门到企业级实战',
    description: '最新 Vue3 Composition API 完全指南，结合TypeScript开发企业级项目',
    coverUrl: 'https://picsum.photos/400/225?random=21',
    teacherId: 2,
    teacher: { id: 2, nickname: '李四', avatar: '' },
    categoryId: 1,
    price: 199,
    originalPrice: 299,
    status: 1,
    buyCount: 2350,
    viewCount: 45600,
    chapterCount: 10,
    lessonCount: 68,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 3,
    title: 'Redis 核心技术与实战',
    description: '深入理解 Redis 原理，掌握高性能缓存方案、分布式锁等企业级应用',
    coverUrl: 'https://picsum.photos/400/225?random=22',
    teacherId: 3,
    teacher: { id: 3, nickname: '王五', avatar: '' },
    categoryId: 5,
    price: 168,
    originalPrice: 268,
    status: 1,
    buyCount: 890,
    viewCount: 15600,
    chapterCount: 8,
    lessonCount: 45,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 4,
    title: 'RocketMQ 消息中间件实战',
    description: '掌握分布式消息队列核心技术，实现异步解耦、削峰填谷',
    coverUrl: 'https://picsum.photos/400/225?random=23',
    teacherId: 1,
    teacher: { id: 1, nickname: '张三', avatar: '' },
    categoryId: 2,
    price: 188,
    originalPrice: 288,
    status: 1,
    buyCount: 670,
    viewCount: 12300,
    chapterCount: 9,
    lessonCount: 52,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 5,
    title: 'MySQL 性能优化与高可用方案',
    description: '数据库调优必备技能，索引优化、慢SQL分析、主从复制',
    coverUrl: 'https://picsum.photos/400/225?random=24',
    teacherId: 4,
    teacher: { id: 4, nickname: '赵六', avatar: '' },
    categoryId: 5,
    price: 158,
    originalPrice: 258,
    status: 1,
    buyCount: 456,
    viewCount: 8900,
    chapterCount: 7,
    lessonCount: 38,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  },
  {
    id: 6,
    title: 'Docker + Kubernetes 云原生实战',
    description: '容器化部署与编排完全指南，DevOps最佳实践',
    coverUrl: 'https://picsum.photos/400/225?random=25',
    teacherId: 5,
    teacher: { id: 5, nickname: '钱七', avatar: '' },
    categoryId: 6,
    price: 268,
    originalPrice: 368,
    status: 1,
    buyCount: 320,
    viewCount: 6700,
    chapterCount: 11,
    lessonCount: 72,
    createdAt: '2024-01-01',
    updatedAt: '2024-01-01'
  }
])

// 分页
const pagination = ref({
  page: 1,
  pageSize: 12,
  total: 100
})

const loading = ref(false)

// 筛选后的课程
const filteredCourses = computed(() => {
  let result = [...courses.value]

  // 分类筛选
  if (categoryId.value && categoryId.value !== 0) {
    result = result.filter(c => c.categoryId === categoryId.value)
  }

  // 关键词筛选
  if (keyword.value) {
    result = result.filter(c =>
      c.title.includes(keyword.value) ||
      c.description.includes(keyword.value)
    )
  }

  // 排序
  switch (sortBy.value) {
    case 'hottest':
      result.sort((a, b) => b.buyCount - a.buyCount)
      break
    case 'price_asc':
      result.sort((a, b) => a.price - b.price)
      break
    case 'price_desc':
      result.sort((a, b) => b.price - a.price)
      break
    default:
      // newest - 默认按创建时间
      break
  }

  return result
})

function selectCategory(id: number) {
  categoryId.value = id === 0 ? undefined : id
  updateRoute()
}

function selectSort(value: string) {
  sortBy.value = value
  updateRoute()
}

function updateRoute() {
  const query: any = {}
  if (categoryId.value) query.categoryId = categoryId.value
  if (keyword.value) query.keyword = keyword.value
  if (sortBy.value !== 'newest') query.sort = sortBy.value
  router.push({ query })
}

function goToCourse(id: number) {
  router.push(`/course/${id}`)
}

function formatCount(count: number): string {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count.toString()
}

// 监听路由变化
watch(() => route.query, (query) => {
  categoryId.value = query.categoryId ? Number(query.categoryId) : undefined
  keyword.value = (query.keyword as string) || ''
  sortBy.value = (query.sort as string) || 'newest'
}, { immediate: true })
</script>

<template>
  <div class="course-list-page">
    <div class="container">
      <!-- 筛选区域 -->
      <div class="filter-section">
        <!-- 分类筛选 -->
        <div class="filter-row">
          <span class="filter-label">分类：</span>
          <div class="filter-options">
            <span
              v-for="cat in categories"
              :key="cat.id"
              class="filter-option"
              :class="{ active: (categoryId || 0) === cat.id }"
              @click="selectCategory(cat.id)"
            >
              {{ cat.name }}
            </span>
          </div>
        </div>

        <!-- 排序 -->
        <div class="filter-row">
          <span class="filter-label">排序：</span>
          <div class="filter-options">
            <span
              v-for="opt in sortOptions"
              :key="opt.value"
              class="filter-option"
              :class="{ active: sortBy === opt.value }"
              @click="selectSort(opt.value)"
            >
              {{ opt.label }}
            </span>
          </div>
        </div>
      </div>

      <!-- 搜索结果提示 -->
      <div v-if="keyword" class="search-result">
        搜索 "<span class="keyword">{{ keyword }}</span>"
        共找到 <span class="count">{{ filteredCourses.length }}</span> 个课程
      </div>

      <!-- 课程列表 -->
      <div class="course-list" v-loading="loading">
        <div
          v-for="course in filteredCourses"
          :key="course.id"
          class="course-card card-hover"
          @click="goToCourse(course.id)"
        >
          <div class="course-cover">
            <img :src="course.coverUrl" :alt="course.title" />
          </div>
          <div class="course-info">
            <h3 class="course-title">{{ course.title }}</h3>
            <p class="course-desc">{{ course.description }}</p>
            <div class="course-stats">
              <span>{{ course.chapterCount }}章 · {{ course.lessonCount }}课时</span>
              <span>{{ formatCount(course.buyCount) }}人学习</span>
            </div>
            <div class="course-footer">
              <div class="teacher">
                <el-avatar :size="24">{{ course.teacher?.nickname?.charAt(0) }}</el-avatar>
                <span>{{ course.teacher?.nickname }}</span>
              </div>
              <div class="price-area">
                <span class="price">¥{{ course.price }}</span>
                <span class="original-price" v-if="course.originalPrice">
                  ¥{{ course.originalPrice }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && filteredCourses.length === 0" description="暂无课程" />

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="filteredCourses.length > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="prev, pager, next"
          background
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.course-list-page {
  padding: 24px 0;
}

/* 筛选区域 */
.filter-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.filter-row:last-child {
  margin-bottom: 0;
}

.filter-label {
  flex-shrink: 0;
  width: 60px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 32px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-option {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 14px;
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.3s;
}

.filter-option:hover {
  background: #f0f5ff;
  color: var(--primary-color);
}

.filter-option.active {
  background: var(--primary-color);
  color: #fff;
}

/* 搜索结果 */
.search-result {
  margin-bottom: 24px;
  font-size: 14px;
  color: var(--text-secondary);
}

.search-result .keyword {
  color: var(--primary-color);
  font-weight: 500;
}

.search-result .count {
  color: var(--primary-color);
  font-weight: 600;
}

/* 课程列表 */
.course-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 400px;
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

.course-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.course-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.teacher {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.price-area {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #ff4d4f;
}

.original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .course-list {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .course-list {
    grid-template-columns: 1fr;
  }
}
</style>
