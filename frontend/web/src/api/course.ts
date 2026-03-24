import { request } from '@/utils/request'
import type { ApiResponse } from '@/types/common'

// 后端：GET /api/v1/courses、GET /api/v1/courses/{id}
export interface Course {
  id: number
  title: string
  category: string
  level: number
  coverUrl?: string
  summary?: string
  description?: string
  price?: number
  originalPrice?: number
  viewCount?: number
  buyCount?: number
  totalLessons?: number
  totalMinutes?: number
  status: number
  publishedAt?: string
  createdAt: string
  updatedAt: string
}

export interface CourseListParams {
  keyword?: string
  category?: string
  level?: number
  status?: number
  sortBy?: 'publishedAt' | 'createdAt' | 'viewCount' | 'buyCount' | 'price'
  order?: 'asc' | 'desc'
  page?: number
  pageSize?: number
}

export interface CourseListResponse {
  items: Course[]
  page: number
  pageSize: number
  total: number
}

// 获取课程列表
export async function getCourseList(params: CourseListParams = {}) {
  const res = await request.get<ApiResponse<CourseListResponse>>('/courses', { params })
  return res.data
}

// 获取课程详情
export async function getCourseDetail(id: string) {
  const res = await request.get<ApiResponse<Course>>(`/courses/${id}`)
  return res.data
}

