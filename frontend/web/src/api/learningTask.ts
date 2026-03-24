import { request } from '@/utils/request'
import type { ApiResponse } from '@/types/common'

// 后端：/api/v1/learners/{learnerId}/learning-tasks
export interface LearningTask {
  id: number
  learnerId: number
  courseId: number
  title: string
  note?: string | null
  status: number
  priority: number
  dueDate?: string | null
  completedAt?: string | null
  createdAt: string
  updatedAt: string
}

export interface LearningTaskListResponse {
  items: LearningTask[]
  page: number
  pageSize: number
  total: number
}

export async function listLearningTasks(
  learnerId: number,
  params: {
    courseId?: number
    status?: number
    priority?: number
    dueFrom?: string
    dueTo?: string
    keyword?: string
    sortBy?: 'dueDate' | 'priority' | 'createdAt' | 'updatedAt'
    order?: 'asc' | 'desc'
    page?: number
    pageSize?: number
  } = {}
) {
  const res = await request.get<ApiResponse<LearningTaskListResponse>>(
    `/learners/${learnerId}/learning-tasks`,
    { params }
  )
  return res.data
}

export async function createLearningTask(
  learnerId: number,
  data: {
    courseId: number
    title: string
    note?: string | null
    priority?: number
    dueDate?: string | null
    status?: number
  }
) {
  const res = await request.post<ApiResponse<LearningTask>>(
    `/learners/${learnerId}/learning-tasks`,
    data
  )
  return res.data
}

export async function patchLearningTask(
  learnerId: number,
  taskId: number,
  data: {
    title?: string
    note?: string | null
    priority?: number
    dueDate?: string | null
    status?: number
  }
) {
  const res = await request.patch<ApiResponse<LearningTask>>(
    `/learners/${learnerId}/learning-tasks/${taskId}`,
    data
  )
  return res.data
}

export async function deleteLearningTask(learnerId: number, taskId: number) {
  const res = await request.delete<ApiResponse<{ deleted: boolean }>>(
    `/learners/${learnerId}/learning-tasks/${taskId}`
  )
  return res.data
}

