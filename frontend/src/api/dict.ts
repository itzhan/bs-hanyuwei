import http from './request'
import type { ApiResponse } from '@/types'

export function getDicts() {
  return http.get<any, ApiResponse<any>>('/api/dicts')
}
