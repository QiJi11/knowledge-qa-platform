import { request } from '@/utils/request'
import type { ApiResponse, PageResponse } from '@/types/common'
import type { Course, Lesson } from '@/types/course'

// 获取我的课程列表
export function getMyCourses(params?: { page?: number; pageSize?: number }) {
  return request.get<ApiResponse<PageResponse<Course>>>('/learn/courses', { params })
}

// 获取播放地址
export function getPlayUrl(lessonId: number) {
  return request.get<ApiResponse<{ url: string; progress: number }>>(`/learn/play/${lessonId}`)
}

// 上报播放进度
export function reportProgress(data: { lessonId: number; progress: number; duration: number }) {
  return request.post<ApiResponse<void>>('/learn/progress', data)
}

// 获取学习历史
export function getLearningHistory(params?: { page?: number; pageSize?: number }) {
  return request.get<ApiResponse<PageResponse<any>>>('/learn/history', { params })
}

// 获取课程学习进度
export function getCourseProgress(courseId: number) {
  return request.get<ApiResponse<{ progress: number; lastLesson: Lesson }>>(`/learn/course/${courseId}/progress`)
}
