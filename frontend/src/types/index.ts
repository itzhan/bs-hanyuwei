/* ======================== API 通用类型 ======================== */

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = unknown> {
  page: number
  size: number
  total: number
  records: T[]
}

/* ======================== 枚举常量 ======================== */

/** 用户角色 */
export const UserRole = { PARENT: 1, ADMIN: 2 } as const
export type UserRoleType = (typeof UserRole)[keyof typeof UserRole]

/** 用户状态 */
export const UserStatus = { ACTIVE: 1, DISABLED: 2 } as const

/** 帖子状态 */
export const PostStatus = {
  PUBLISHED: 1,
  PENDING: 2,
  HIDDEN: 3,
  REJECTED: 4,
  BLOCKED: 5,
} as const
export type PostStatusType = (typeof PostStatus)[keyof typeof PostStatus]

/** 评论状态 */
export const CommentStatus = { PUBLISHED: 1, HIDDEN: 2, DELETED: 3 } as const

/** 性别 */
export const Gender = { UNKNOWN: 0, MALE: 1, FEMALE: 2 } as const

/** 成长日志类型 */
export const LogType = {
  FEEDING: 1,
  SLEEP: 2,
  ILLNESS: 3,
  MILESTONE: 4,
  MEASUREMENT: 5,
  OTHER: 6,
} as const

export const LogTypeLabels: Record<number, string> = {
  1: '喂养',
  2: '睡眠',
  3: '疾病',
  4: '里程碑',
  5: '体检',
  6: '其他',
}

/** 成长指标类型 */
export const MetricType = {
  HEIGHT: 1,
  WEIGHT: 2,
  HEAD_CIRCUMFERENCE: 3,
  TEMPERATURE: 4,
} as const

export const MetricTypeLabels: Record<number, string> = {
  1: '身高 (cm)',
  2: '体重 (kg)',
  3: '头围 (cm)',
  4: '体温 (°C)',
}

/** 举报原因 */
export const ReportReason = {
  SPAM: 1,
  ABUSE: 2,
  PRIVACY: 3,
  ILLEGAL: 4,
  OTHER: 5,
} as const

export const ReportReasonLabels: Record<number, string> = {
  1: '垃圾广告',
  2: '辱骂攻击',
  3: '侵犯隐私',
  4: '违法内容',
  5: '其他',
}

/* ======================== 实体类型 ======================== */

export interface User {
  id: number
  username: string
  role: number
  status: number
  displayName: string | null
  avatarPath: string | null
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
}

export interface Baby {
  id: number
  userId: number
  name: string
  gender: number | null
  birthday: string | null
  relation: string | null
  avatarPath: string | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface GrowthLog {
  id: number
  userId: number
  babyId: number
  logType: number
  title: string | null
  content: string | null
  logTime: string
  createdAt: string
  updatedAt: string
}

export interface GrowthMetric {
  id: number
  userId: number
  babyId: number
  sourceLogId: number | null
  metricType: number
  metricValue: number
  unit: string | null
  recordedAt: string
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface PostCategory {
  id: number
  name: string
  description: string | null
  sortOrder: number
  status: number
}

export interface PostTopic {
  id: number
  name: string
  description: string | null
  status: number
}

export interface PostTag {
  id: number
  name: string
}

export interface Post {
  id: number
  userId: number
  categoryId: number | null
  topicId: number | null
  title: string
  content: string
  status: number
  commentCount: number
  createdAt: string
  /* 后端 PostVO 返回 tagIds (Long 数组)，不含关联对象 */
  tagIds?: number[]
}

export interface PostComment {
  id: number
  postId: number
  userId: number
  parentId: number | null
  replyToUserId: number | null
  content: string
  status: number
  createdAt: string
  /* 后端 CommentVO 不含 user/replyToUser 关联对象 */
}

export interface CommunityOverview {
  postDaily: Array<{ date: string; count: number }>
  commentDaily: Array<{ date: string; count: number }>
}

/* ======================== 请求 DTO ======================== */

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  displayName: string
}

export interface BabyDTO {
  name: string
  gender?: number | null
  birthday?: string | null
  relation?: string | null
  avatarPath?: string | null
  note?: string | null
}

export interface GrowthLogDTO {
  babyId: number
  logType: number
  title?: string
  content?: string
  logTime: string
}

export interface GrowthMetricDTO {
  babyId: number
  metricType: number
  metricValue: number
  unit?: string
  recordedAt: string
  note?: string
}

export interface PostDTO {
  title: string
  content: string
  categoryId?: number | null
  topicId?: number | null
  tagIds?: number[]
}

export interface CommentDTO {
  content: string
  parentId?: number | null
  replyToUserId?: number | null
}

export interface ReportDTO {
  postId?: number | null
  commentId?: number | null
  reason: number
  reasonDesc?: string
}

/* ======================== 认证响应 ======================== */

export interface LoginResponse {
  token: string
  tokenType: string
  user: User
}
