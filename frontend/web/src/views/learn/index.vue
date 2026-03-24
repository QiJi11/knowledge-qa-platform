<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Chapter, Lesson } from '@/types/course'

const route = useRoute()
const router = useRouter()

const lessonId = computed(() => Number(route.params.lessonId))

// 模拟课程数据
const courseTitle = ref('Spring Cloud Alibaba 微服务架构实战')
const currentLesson = ref<Lesson>({
  id: 1,
  chapterId: 1,
  courseId: 1,
  title: '1.1 课程介绍与学习路线',
  videoUrl: 'https://www.w3schools.com/html/mov_bbb.mp4',
  duration: 600,
  isFree: true,
  sortOrder: 1
})

const chapters = ref<Chapter[]>([
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
      { id: 6, chapterId: 2, courseId: 1, title: '2.3 配置中心实战', duration: 1100, isFree: false, sortOrder: 3 }
    ]
  }
])

const videoRef = ref<HTMLVideoElement>()
const isPlaying = ref(false)
const progress = ref(0)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(0.8)
const playbackRate = ref(1)
const isFullscreen = ref(false)
const showCatalog = ref(true)

// 播放速度选项
const rateOptions = [0.5, 0.75, 1, 1.25, 1.5, 2]

function formatTime(seconds: number): string {
  const min = Math.floor(seconds / 60)
  const sec = Math.floor(seconds % 60)
  return `${min}:${sec.toString().padStart(2, '0')}`
}

function togglePlay() {
  if (!videoRef.value) return
  if (isPlaying.value) {
    videoRef.value.pause()
  } else {
    videoRef.value.play()
  }
}

function handleTimeUpdate() {
  if (!videoRef.value) return
  currentTime.value = videoRef.value.currentTime
  progress.value = (currentTime.value / duration.value) * 100
}

function handleLoadedMetadata() {
  if (!videoRef.value) return
  duration.value = videoRef.value.duration
}

function handleSeek(val: number) {
  if (!videoRef.value) return
  videoRef.value.currentTime = (val / 100) * duration.value
}

function handleVolumeChange(val: number) {
  if (!videoRef.value) return
  videoRef.value.volume = val
}

function changeRate(rate: number) {
  if (!videoRef.value) return
  videoRef.value.playbackRate = rate
  playbackRate.value = rate
}

function toggleFullscreen() {
  const container = document.querySelector('.player-container') as HTMLElement
  if (!document.fullscreenElement) {
    container?.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

function switchLesson(lesson: Lesson) {
  currentLesson.value = lesson
  router.replace(`/learn/${lesson.id}`)
  // 重新加载视频
  if (videoRef.value) {
    videoRef.value.load()
    videoRef.value.play()
  }
}

function goBack() {
  router.push(`/course/${currentLesson.value.courseId}`)
}

// 键盘快捷键
function handleKeydown(e: KeyboardEvent) {
  if (!videoRef.value) return
  switch (e.key) {
    case ' ':
      e.preventDefault()
      togglePlay()
      break
    case 'ArrowLeft':
      videoRef.value.currentTime -= 5
      break
    case 'ArrowRight':
      videoRef.value.currentTime += 5
      break
    case 'ArrowUp':
      volume.value = Math.min(1, volume.value + 0.1)
      handleVolumeChange(volume.value)
      break
    case 'ArrowDown':
      volume.value = Math.max(0, volume.value - 0.1)
      handleVolumeChange(volume.value)
      break
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div class="learn-page">
    <!-- 顶部导航 -->
    <header class="learn-header">
      <div class="header-left">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <span class="course-title">{{ courseTitle }}</span>
      </div>
      <div class="header-right">
        <el-button text @click="showCatalog = !showCatalog">
          <el-icon><Menu /></el-icon>
          目录
        </el-button>
      </div>
    </header>

    <div class="learn-content">
      <!-- 播放器区域 -->
      <div class="player-section" :class="{ 'full-width': !showCatalog }">
        <div class="player-container">
          <video
            ref="videoRef"
            :src="currentLesson.videoUrl"
            @timeupdate="handleTimeUpdate"
            @loadedmetadata="handleLoadedMetadata"
            @play="isPlaying = true"
            @pause="isPlaying = false"
            @click="togglePlay"
          ></video>

          <!-- 播放控制栏 -->
          <div class="player-controls">
            <el-button text @click="togglePlay" class="play-btn">
              <el-icon :size="24">
                <VideoPlay v-if="!isPlaying" />
                <VideoPause v-else />
              </el-icon>
            </el-button>

            <span class="time-display">
              {{ formatTime(currentTime) }} / {{ formatTime(duration) }}
            </span>

            <el-slider
              v-model="progress"
              :show-tooltip="false"
              class="progress-bar"
              @change="handleSeek"
            />

            <el-dropdown trigger="click">
              <el-button text class="rate-btn">
                {{ playbackRate }}x
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="rate in rateOptions"
                    :key="rate"
                    @click="changeRate(rate)"
                  >
                    {{ rate }}x
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <div class="volume-control">
              <el-icon><Microphone /></el-icon>
              <el-slider
                v-model="volume"
                :max="1"
                :step="0.1"
                :show-tooltip="false"
                class="volume-slider"
                @input="handleVolumeChange"
              />
            </div>

            <el-button text @click="toggleFullscreen">
              <el-icon :size="20">
                <FullScreen v-if="!isFullscreen" />
                <Close v-else />
              </el-icon>
            </el-button>
          </div>
        </div>

        <!-- 当前课时信息 -->
        <div class="lesson-info">
          <h2>{{ currentLesson.title }}</h2>
        </div>
      </div>

      <!-- 课程目录 -->
      <aside class="catalog-sidebar" v-show="showCatalog">
        <h3>课程目录</h3>
        <div class="catalog-content">
          <el-collapse>
            <el-collapse-item
              v-for="chapter in chapters"
              :key="chapter.id"
              :name="chapter.id"
            >
              <template #title>
                <span class="chapter-title">{{ chapter.title }}</span>
              </template>
              <div class="lesson-list">
                <div
                  v-for="lesson in chapter.lessons"
                  :key="lesson.id"
                  class="lesson-item"
                  :class="{ active: lesson.id === currentLesson.id }"
                  @click="switchLesson(lesson)"
                >
                  <el-icon class="play-icon">
                    <VideoPlay v-if="lesson.id !== currentLesson.id" />
                    <VideoPause v-else />
                  </el-icon>
                  <span class="lesson-title">{{ lesson.title }}</span>
                  <span class="lesson-duration">{{ formatTime(lesson.duration) }}</span>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.learn-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #000;
}

/* Header */
.learn-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #1a1a1a;
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.course-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

/* Content */
.learn-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.player-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.player-section.full-width {
  width: 100%;
}

/* Player */
.player-container {
  position: relative;
  flex: 1;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.player-container video {
  max-width: 100%;
  max-height: 100%;
  cursor: pointer;
}

.player-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}

.play-btn {
  color: #fff;
}

.time-display {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  min-width: 100px;
}

.progress-bar {
  flex: 1;
}

.progress-bar :deep(.el-slider__runway) {
  background: rgba(255, 255, 255, 0.2);
}

.progress-bar :deep(.el-slider__bar) {
  background: var(--primary-color);
}

.rate-btn {
  color: #fff;
  font-size: 13px;
}

.volume-control {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
}

.volume-slider {
  width: 80px;
}

/* Lesson Info */
.lesson-info {
  padding: 16px 20px;
  background: #1a1a1a;
  color: #fff;
}

.lesson-info h2 {
  font-size: 16px;
  font-weight: 500;
}

/* Sidebar */
.catalog-sidebar {
  width: 360px;
  background: #1a1a1a;
  color: #fff;
  display: flex;
  flex-direction: column;
  border-left: 1px solid #333;
}

.catalog-sidebar h3 {
  padding: 16px 20px;
  font-size: 16px;
  border-bottom: 1px solid #333;
}

.catalog-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.catalog-content :deep(.el-collapse) {
  border: none;
}

.catalog-content :deep(.el-collapse-item__header) {
  background: transparent;
  border: none;
  color: #fff;
}

.catalog-content :deep(.el-collapse-item__wrap) {
  background: transparent;
  border: none;
}

.catalog-content :deep(.el-collapse-item__content) {
  padding: 0;
}

.chapter-title {
  font-size: 14px;
}

.lesson-list {
  padding: 8px 0;
}

.lesson-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.lesson-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.lesson-item.active {
  background: rgba(59, 130, 246, 0.2);
  color: var(--primary-color);
}

.lesson-item .play-icon {
  font-size: 16px;
}

.lesson-item .lesson-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lesson-item .lesson-duration {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}
</style>
