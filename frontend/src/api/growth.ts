import http from './request'
import type { ApiResponse, PageResult, GrowthLog, GrowthMetric, GrowthLogDTO, GrowthMetricDTO } from '@/types'

/* ---- 成长日志 ---- */

export function getGrowthLogs(params?: {
  page?: number
  size?: number
  babyId?: number
  logType?: number
  startTime?: string
  endTime?: string
  userId?: number
}) {
  return http.get<any, ApiResponse<PageResult<GrowthLog>>>('/api/growth-logs', { params })
}

export function getGrowthLog(id: number) {
  return http.get<any, ApiResponse<GrowthLog>>(`/api/growth-logs/${id}`)
}

export function createGrowthLog(data: GrowthLogDTO) {
  return http.post<any, ApiResponse<GrowthLog>>('/api/growth-logs', data)
}

export function updateGrowthLog(id: number, data: GrowthLogDTO) {
  return http.put<any, ApiResponse<GrowthLog>>(`/api/growth-logs/${id}`, data)
}

export function deleteGrowthLog(id: number) {
  return http.delete<any, ApiResponse<void>>(`/api/growth-logs/${id}`)
}

/* ---- 成长指标 ---- */

export function getGrowthMetrics(params?: {
  page?: number
  size?: number
  babyId?: number
  metricType?: number
  startTime?: string
  endTime?: string
  userId?: number
}) {
  return http.get<any, ApiResponse<PageResult<GrowthMetric>>>('/api/growth-metrics', { params })
}

export function getGrowthMetric(id: number) {
  return http.get<any, ApiResponse<GrowthMetric>>(`/api/growth-metrics/${id}`)
}

export function createGrowthMetric(data: GrowthMetricDTO) {
  return http.post<any, ApiResponse<GrowthMetric>>('/api/growth-metrics', data)
}

export function updateGrowthMetric(id: number, data: GrowthMetricDTO) {
  return http.put<any, ApiResponse<GrowthMetric>>(`/api/growth-metrics/${id}`, data)
}

export function deleteGrowthMetric(id: number) {
  return http.delete<any, ApiResponse<void>>(`/api/growth-metrics/${id}`)
}

/* ---- 统计 ---- */

export function getBabyMetricStats(
  babyId: number,
  params?: { metricType?: number; startTime?: string; endTime?: string },
) {
  return http.get<any, ApiResponse<GrowthMetric[]>>(`/api/stats/baby/${babyId}/metrics`, { params })
}

export function getCommunityOverview(days?: number) {
  return http.get<any, ApiResponse<any>>('/api/stats/community/overview', { params: { days } })
}
