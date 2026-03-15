import { request } from '../request';

export interface PageResult<T> {
  page: number;
  size: number;
  total: number;
  records: T[];
}

export interface UserItem {
  id: number;
  username: string;
  role: number;
  status: number;
  displayName?: string;
  avatarPath?: string;
  createdAt?: string;
}

export interface BabyItem {
  id: number;
  userId: number;
  name: string;
  gender: number | null;
  birthday?: string;
  relation?: string;
  avatarPath?: string;
  note?: string;
  createdAt?: string;
}

export interface GrowthLogItem {
  id: number;
  userId: number;
  babyId: number;
  logType: number;
  title?: string;
  content?: string;
  logTime: string;
  createdAt?: string;
}

export interface GrowthMetricItem {
  id: number;
  userId: number;
  babyId: number;
  sourceLogId?: number;
  metricType: number;
  metricValue: number;
  unit?: string;
  recordedAt: string;
  note?: string;
  createdAt?: string;
}

export interface PostItem {
  id: number;
  userId: number;
  categoryId?: number;
  topicId?: number;
  title: string;
  content?: string;
  status: number;
  commentCount: number;
  createdAt?: string;
  tagIds?: number[];
}

export interface CommentItem {
  id: number;
  postId: number;
  userId: number;
  parentId?: number;
  replyToUserId?: number;
  content: string;
  status: number;
  createdAt?: string;
}

export interface ReportItem {
  id: number;
  reporterId: number;
  postId?: number;
  commentId?: number;
  reason: number;
  reasonDesc?: string;
  status: number;
  handledBy?: number;
  handledAt?: string;
  handleResult?: number;
  handleNote?: string;
  createdAt?: string;
}

export interface CategoryItem {
  id: number;
  name: string;
  description?: string;
  sortOrder: number;
  status: number;
}

export interface TopicItem {
  id: number;
  name: string;
  description?: string;
  status: number;
}

export interface TagItem {
  id: number;
  name: string;
}

export function fetchDicts() {
  return request<Record<string, any>>({ url: '/dicts' });
}

export function fetchUsers(params: Record<string, any>) {
  return request<PageResult<UserItem>>({ url: '/admin/users', params });
}

export function createUser(data: Record<string, any>) {
  return request<UserItem>({ url: '/admin/users', method: 'post', data });
}

export function updateUser(id: number, data: Record<string, any>) {
  return request<UserItem>({ url: `/admin/users/${id}`, method: 'put', data });
}

export function updateUserStatus(id: number, status: number) {
  return request<void>({ url: `/admin/users/${id}/status`, method: 'patch', data: { status } });
}

export function fetchBabies(params: Record<string, any>) {
  return request<PageResult<BabyItem>>({ url: '/babies', params });
}

export function createBaby(data: Record<string, any>) {
  return request<BabyItem>({ url: '/babies', method: 'post', data });
}

export function updateBaby(id: number, data: Record<string, any>) {
  return request<BabyItem>({ url: `/babies/${id}`, method: 'put', data });
}

export function deleteBaby(id: number) {
  return request<void>({ url: `/babies/${id}`, method: 'delete' });
}

export function fetchGrowthLogs(params: Record<string, any>) {
  return request<PageResult<GrowthLogItem>>({ url: '/growth-logs', params });
}

export function createGrowthLog(data: Record<string, any>) {
  return request<GrowthLogItem>({ url: '/growth-logs', method: 'post', data });
}

export function updateGrowthLog(id: number, data: Record<string, any>) {
  return request<GrowthLogItem>({ url: `/growth-logs/${id}`, method: 'put', data });
}

export function deleteGrowthLog(id: number) {
  return request<void>({ url: `/growth-logs/${id}`, method: 'delete' });
}

export function fetchGrowthMetrics(params: Record<string, any>) {
  return request<PageResult<GrowthMetricItem>>({ url: '/growth-metrics', params });
}

export function createGrowthMetric(data: Record<string, any>) {
  return request<GrowthMetricItem>({ url: '/growth-metrics', method: 'post', data });
}

export function updateGrowthMetric(id: number, data: Record<string, any>) {
  return request<GrowthMetricItem>({ url: `/growth-metrics/${id}`, method: 'put', data });
}

export function deleteGrowthMetric(id: number) {
  return request<void>({ url: `/growth-metrics/${id}`, method: 'delete' });
}

export function fetchAdminPosts(params: Record<string, any>) {
  return request<PageResult<PostItem>>({ url: '/admin/posts', params });
}

export function updatePostStatus(id: number, status: number, reason?: string) {
  return request<void>({ url: `/admin/posts/${id}/status`, method: 'patch', data: { status, reason } });
}

export function fetchAdminComments(params: Record<string, any>) {
  return request<PageResult<CommentItem>>({ url: '/admin/comments', params });
}

export function updateCommentStatus(id: number, status: number, reason?: string) {
  return request<void>({ url: `/admin/comments/${id}/status`, method: 'patch', data: { status, reason } });
}

export function fetchAdminReports(params: Record<string, any>) {
  return request<PageResult<ReportItem>>({ url: '/admin/reports', params });
}

export function handleReport(id: number, data: Record<string, any>) {
  return request<void>({ url: `/admin/reports/${id}/handle`, method: 'patch', data });
}

export function fetchAdminCategories() {
  return request<CategoryItem[]>({ url: '/admin/categories' });
}

export function createCategory(data: Record<string, any>) {
  return request<CategoryItem>({ url: '/admin/categories', method: 'post', data });
}

export function updateCategory(id: number, data: Record<string, any>) {
  return request<CategoryItem>({ url: `/admin/categories/${id}`, method: 'put', data });
}

export function deleteCategory(id: number) {
  return request<void>({ url: `/admin/categories/${id}`, method: 'delete' });
}

export function fetchAdminTopics() {
  return request<TopicItem[]>({ url: '/admin/topics' });
}

export function createTopic(data: Record<string, any>) {
  return request<TopicItem>({ url: '/admin/topics', method: 'post', data });
}

export function updateTopic(id: number, data: Record<string, any>) {
  return request<TopicItem>({ url: `/admin/topics/${id}`, method: 'put', data });
}

export function deleteTopic(id: number) {
  return request<void>({ url: `/admin/topics/${id}`, method: 'delete' });
}

export function fetchAdminTags() {
  return request<TagItem[]>({ url: '/admin/tags' });
}

export function createTag(data: Record<string, any>) {
  return request<TagItem>({ url: '/admin/tags', method: 'post', data });
}

export function updateTag(id: number, data: Record<string, any>) {
  return request<TagItem>({ url: `/admin/tags/${id}`, method: 'put', data });
}

export function deleteTag(id: number) {
  return request<void>({ url: `/admin/tags/${id}`, method: 'delete' });
}

export function fetchCommunityOverview(days: number) {
  return request<{ postDaily: any[]; commentDaily: any[] }>({ url: '/stats/community/overview', params: { days } });
}

export function fetchDashboardStats() {
  return request<{
    userCount: number;
    postCount: number;
    babyCount: number;
    commentCount: number;
    categoryStats: { name: string; value: number }[];
    recentPosts: { id: number; title: string; authorName: string; status: number; createdAt: string }[];
    postDaily: { day: string; total: number }[];
    commentDaily: { day: string; total: number }[];
  }>({ url: '/stats/dashboard' });
}
