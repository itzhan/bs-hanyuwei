import http from './request'
import type { ApiResponse, LoginDTO, RegisterDTO, LoginResponse, User } from '@/types'

export function login(data: LoginDTO) {
  return http.post<any, ApiResponse<LoginResponse>>('/api/auth/login', data)
}

export function register(data: RegisterDTO) {
  return http.post<any, ApiResponse<User>>('/api/auth/register', data)
}

export function getMe() {
  return http.get<any, ApiResponse<User>>('/api/auth/me')
}
