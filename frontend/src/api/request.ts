import axios from 'axios'
import type { ApiResponse } from '@/types'

const http = axios.create({
  baseURL: '',
  timeout: 15000,
})

/* ---------- 请求拦截器 ---------- */
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/* ---------- 响应拦截器 ---------- */
http.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse
    if (res.code !== 0) {
      // 业务错误
      const msg = res.message || '请求失败'
      window.$message?.error(msg)
      return Promise.reject(new Error(msg))
    }
    return res as any
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.hash = '#/login'
        window.$message?.error('登录已过期，请重新登录')
      } else if (status === 403) {
        window.$message?.error('没有权限执行此操作')
      } else if (status === 404) {
        window.$message?.error('请求的资源不存在')
      } else {
        window.$message?.error(error.response.data?.message || '服务器异常')
      }
    } else {
      window.$message?.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  },
)

export default http

/* 扩展 window 类型以支持 naive-ui 消息 */
declare global {
  interface Window {
    $message: {
      success: (msg: string) => void
      error: (msg: string) => void
      warning: (msg: string) => void
      info: (msg: string) => void
    } | null
  }
}
