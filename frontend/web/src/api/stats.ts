import { request } from '@/utils/request'
import type { ApiResponse } from '@/types/common'

export interface OverviewStats {
  dateRange: { from: string; to: string }
  summary: {
    totalTasks: number
    completedTasks: number
    completionRate: number
    overdueTasks: number
    inProgressTasks: number
  }
  byStatus: {
    pending: number
    inProgress: number
    completed: number
    cancelled: number
  }
  byPriority: {
    low: number
    medium: number
    high: number
  }
}

export interface ByCourseStats {
  dateRange: { from: string; to: string }
  courses: Array<{
    courseId: number
    courseTitle: string
    category: string
    totalTasks: number
    completedTasks: number
    completionRate: number
    overdueTasks: number
    avgCompletionDays?: number | null
  }>
}

export interface TrendStats {
  dateRange: { from: string; to: string }
  granularity: string
  dataPoints: Array<{
    date: string
    createdCount: number
    completedCount: number
  }>
}

export async function getOverviewStats(
  learnerId: number,
  params?: { from?: string; to?: string }
) {
  const res = await request.get<ApiResponse<OverviewStats>>(
    `/learners/${learnerId}/stats/overview`,
    { params }
  )
  return res.data
}

export async function getByCourseStats(
  learnerId: number,
  params?: { from?: string; to?: string; sortBy?: string; order?: string }
) {
  const res = await request.get<ApiResponse<ByCourseStats>>(
    `/learners/${learnerId}/stats/by-course`,
    { params }
  )
  return res.data
}

export async function getTrendStats(
  learnerId: number,
  params?: { from?: string; to?: string; granularity?: string }
) {
  const res = await request.get<ApiResponse<TrendStats>>(
    `/learners/${learnerId}/stats/trend`,
    { params }
  )
  return res.data
}

