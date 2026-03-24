<template>
  <router-link :to="`/course/${course.id}`" class="course-card">
    <div class="course-cover">
      <img v-if="course.coverUrl" :src="course.coverUrl" :alt="course.title" loading="lazy" decoding="async" class="cover-image" />
      <div v-else class="cover-placeholder" :style="{ background: randomGradient }"></div>
      <div class="cover-title-overlay">{{ course.title }}</div>
    </div>
    <div class="course-info">
      <h3 class="course-title">{{ course.title }}</h3>
      <p class="course-summary">{{ course.summary || '暂无简介' }}</p>
      <div class="course-price-row">
        <span class="price" v-if="course.price && course.price > 0">¥{{ course.price }}</span>
        <span class="price free" v-else>免费</span>
        <span class="original-price" v-if="course.originalPrice && course.originalPrice > course.price">¥{{ course.originalPrice }}</span>
      </div>
      <div class="course-footer">
        <span class="meta-item">{{ course.category }} · L{{ course.level }}</span>
        <span class="meta-item">
          <span class="hot-icon">🔥</span>
          {{ formatCount(course.viewCount || 0) }}人看过 · {{ formatCount(course.buyCount || 0) }}人购买
        </span>
      </div>
    </div>
  </router-link>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Course } from '@/api/course'

const props = defineProps<{
  course: Course
}>()

const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
]
const randomGradient = computed(() => gradients[Math.floor(Math.random() * gradients.length)])

function formatCount(n: number): string {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}
</script>

<style scoped>
.course-card {
  display: block;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--card-shadow);
  transition: all 0.3s;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--card-shadow-hover);
}

.course-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  overflow: hidden;
}

.cover-placeholder {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
}

.cover-image {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  object-fit: cover;
  filter: brightness(0.85);
  transition: transform 0.3s;
}

.course-card:hover .cover-image {
  transform: scale(1.05);
}

.cover-title-overlay {
  position: absolute;
  bottom: 0; left: 0;
  width: 100%;
  padding: 40px 16px 16px;
  background: linear-gradient(to bottom, transparent, rgba(0,0,0,0.8));
  color: white;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.course-info {
  padding: 16px;
}

.course-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  min-height: 50px;
}

.course-summary {
  font-size: 13px;
  color: var(--text-tertiary);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 价格行 */
.course-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: #ff4d4f;
}

.price.free {
  color: #52c41a;
  font-size: 16px;
}

.original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.meta-item {
  font-size: 12px;
  color: var(--text-tertiary);
}

.hot-icon {
  font-size: 12px;
}
</style>
