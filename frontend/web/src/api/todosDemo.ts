import { request } from '@/utils/request'
import type { ApiResponse } from '@/types/common'

export type TodoItem = {
  id: string
  title: string
  done: boolean
  createdAt: string
  updatedAt: string
}

export function listTodos() {
  return request.get<ApiResponse<{ items: TodoItem[] }>>('/todos')
}

export function createTodo(data: { title: string; done?: boolean }) {
  return request.post<ApiResponse<{ item: TodoItem }>>('/todos', data)
}

export function patchTodo(id: string, data: { title?: string; done?: boolean }) {
  return request.patch<ApiResponse<{ item: TodoItem }>>(`/todos/${id}`, data)
}

export function deleteTodo(id: string) {
  return request.delete<ApiResponse<{ remaining: number }>>(`/todos/${id}`)
}
