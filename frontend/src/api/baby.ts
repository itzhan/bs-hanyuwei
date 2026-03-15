import http from './request'
import type { ApiResponse, PageResult, Baby, BabyDTO } from '@/types'

export function getBabies(params?: { page?: number; size?: number; userId?: number }) {
  return http.get<any, ApiResponse<PageResult<Baby>>>('/api/babies', { params })
}

export function getBaby(id: number) {
  return http.get<any, ApiResponse<Baby>>(`/api/babies/${id}`)
}

export function createBaby(data: BabyDTO) {
  return http.post<any, ApiResponse<Baby>>('/api/babies', data)
}

export function updateBaby(id: number, data: BabyDTO) {
  return http.put<any, ApiResponse<Baby>>(`/api/babies/${id}`, data)
}

export function deleteBaby(id: number) {
  return http.delete<any, ApiResponse<void>>(`/api/babies/${id}`)
}
