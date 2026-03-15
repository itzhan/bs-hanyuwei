import http from './request'
import type { ApiResponse } from '@/types'

export function uploadFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post<any, ApiResponse<{ url: string }>>('/api/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
