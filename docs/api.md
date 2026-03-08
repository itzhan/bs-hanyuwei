# API 文档

## 概览
- Base URL: `http://localhost:8080`
- 认证方式：JWT（登录后返回 token，Header：`Authorization: Bearer <token>`）
- Content-Type: `application/json`
- 分页：`page/size`

## 通用返回
```json
{ "code": 0, "message": "ok", "data": {} }
```

## 错误码
- 400 参数错误
- 401 未登录
- 403 无权限
- 404 不存在
- 500 服务器异常

## 认证 Auth
### 登录
- POST `/api/auth/login`
- Body
```json
{ "username": "admin", "password": "admin123" }
```
- Response
```json
{ "code":0, "data": { "token":"xxx", "tokenType":"Bearer", "user": {"id":1,"username":"admin","role":2} } }
```

### 注册（家长）
- POST `/api/auth/register`
- Body
```json
{ "username":"user1", "password":"123456", "displayName":"妈妈" }
```

### 当前用户
- GET `/api/auth/me`

## 字典
- GET `/api/dicts`

## 用户管理（管理员）
- GET `/api/admin/users?page=1&size=10&keyword=&role=&status=`
- POST `/api/admin/users`
- PUT `/api/admin/users/{id}`
- PATCH `/api/admin/users/{id}/status`

## 宝宝档案
- GET `/api/babies?page=1&size=10&userId=`（管理员可查全部）
- GET `/api/babies/{id}`
- POST `/api/babies`
- PUT `/api/babies/{id}`
- DELETE `/api/babies/{id}`

## 成长日志
- GET `/api/growth-logs?page=1&size=10&babyId=&logType=&startTime=&endTime=&userId=`
- GET `/api/growth-logs/{id}`
- POST `/api/growth-logs`
- PUT `/api/growth-logs/{id}`
- DELETE `/api/growth-logs/{id}`

## 成长指标
- GET `/api/growth-metrics?page=1&size=10&babyId=&metricType=&startTime=&endTime=&userId=`
- GET `/api/growth-metrics/{id}`
- POST `/api/growth-metrics`
- PUT `/api/growth-metrics/{id}`
- DELETE `/api/growth-metrics/{id}`

## 统计
- GET `/api/stats/baby/{babyId}/metrics?metricType=&startTime=&endTime=`
- GET `/api/stats/community/overview?days=7`

## 帖子
- GET `/api/posts?page=1&size=10&categoryId=&topicId=&tagId=&keyword=&mine=false`
- GET `/api/posts/{id}`
- POST `/api/posts`
- PUT `/api/posts/{id}`（家长修改后状态回到待审核）
- DELETE `/api/posts/{id}`

## 分类/话题/标签
- GET `/api/categories`
- GET `/api/topics`
- GET `/api/tags`

## 评论
- GET `/api/posts/{postId}/comments?page=1&size=10`
- POST `/api/posts/{postId}/comments`
- DELETE `/api/comments/{id}`

## 举报
- POST `/api/reports`

## 管理端（审核/配置）
### 帖子审核
- GET `/api/admin/posts?page=1&size=10&status=&userId=&categoryId=&keyword=`
- PATCH `/api/admin/posts/{id}/status`

### 评论审核
- GET `/api/admin/comments?page=1&size=10&postId=&userId=&status=&keyword=`
- PATCH `/api/admin/comments/{id}/status`

### 举报处理
- GET `/api/admin/reports?page=1&size=10&status=`
- PATCH `/api/admin/reports/{id}/handle`

### 分类/话题/标签管理
- GET `/api/admin/categories` / POST / PUT / DELETE
- GET `/api/admin/topics` / POST / PUT / DELETE
- GET `/api/admin/tags` / POST / PUT / DELETE

## 文件上传（本地存储）
- POST `/api/files/upload`（form-data, file）
- 返回字段 `url` 可直接访问 `/uploads/{fileName}`

## 样例：创建帖子
Request:
```json
{ "title":"喂养小技巧", "content":"...", "categoryId":1, "topicId":1, "tagIds":[1,2] }
```
Response:
```json
{ "code":0, "data": { "id":10, "status":2 } }
```
