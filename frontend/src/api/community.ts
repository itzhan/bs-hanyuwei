import http from './request'
import type {
  ApiResponse, PageResult, Post, PostComment,
  PostCategory, PostTopic, PostTag,
  PostDTO, CommentDTO, ReportDTO,
} from '@/types'

/* ---- 帖子 ---- */

export function getPosts(params?: {
  page?: number
  size?: number
  categoryId?: number
  topicId?: number
  tagId?: number
  keyword?: string
  mine?: boolean
}) {
  return http.get<any, ApiResponse<PageResult<Post>>>('/api/posts', { params })
}

export function getPost(id: number) {
  return http.get<any, ApiResponse<Post>>(`/api/posts/${id}`)
}

export function createPost(data: PostDTO) {
  return http.post<any, ApiResponse<Post>>('/api/posts', data)
}

export function updatePost(id: number, data: PostDTO) {
  return http.put<any, ApiResponse<Post>>(`/api/posts/${id}`, data)
}

export function deletePost(id: number) {
  return http.delete<any, ApiResponse<void>>(`/api/posts/${id}`)
}

/* ---- 分类 / 话题 / 标签 ---- */

export function getCategories() {
  return http.get<any, ApiResponse<PostCategory[]>>('/api/categories')
}

export function getTopics() {
  return http.get<any, ApiResponse<PostTopic[]>>('/api/topics')
}

export function getTags() {
  return http.get<any, ApiResponse<PostTag[]>>('/api/tags')
}

/* ---- 评论 ---- */

export function getComments(postId: number, params?: { page?: number; size?: number }) {
  return http.get<any, ApiResponse<PageResult<PostComment>>>(`/api/posts/${postId}/comments`, { params })
}

export function createComment(postId: number, data: CommentDTO) {
  return http.post<any, ApiResponse<PostComment>>(`/api/posts/${postId}/comments`, data)
}

export function deleteComment(id: number) {
  return http.delete<any, ApiResponse<void>>(`/api/comments/${id}`)
}

/* ---- 举报 ---- */

export function createReport(data: ReportDTO) {
  return http.post<any, ApiResponse<void>>('/api/reports', data)
}
