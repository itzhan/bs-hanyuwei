#!/bin/bash
# ============================================================================
#  前端-后端 API 全量联调测试脚本
#  覆盖所有 22 个 Controller 的全部接口
#  用法: chmod +x api-test.sh && ./api-test.sh
# ============================================================================

BASE_URL="http://localhost:8080/api"
PASS=0
FAIL=0
TOTAL=0
FAILED_APIS=()

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ── 工具函数 ──────────────────────────────────────────────────────────────────

# 断言 HTTP 状态码 + 业务 code
# 用法: assert "测试名" HTTP_CODE RESPONSE [期望code]
assert() {
  local name="$1"
  local http_code="$2"
  local body="$3"
  local expect_code="${4:-0}"
  TOTAL=$((TOTAL + 1))

  local biz_code
  biz_code=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('code','?'))" 2>/dev/null || echo "?")

  if [[ "$http_code" == "200" && "$biz_code" == "$expect_code" ]]; then
    PASS=$((PASS + 1))
    printf "${GREEN}  ✅ %-50s HTTP=%s  code=%s${NC}\n" "$name" "$http_code" "$biz_code"
  else
    FAIL=$((FAIL + 1))
    FAILED_APIS+=("$name (HTTP=$http_code, code=$biz_code)")
    printf "${RED}  ❌ %-50s HTTP=%s  code=%s${NC}\n" "$name" "$http_code" "$biz_code"
    # 打印前200字符的响应体帮助排错
    echo "     └─ ${body:0:200}"
  fi
}

# 发送请求并捕获 HTTP 状态码和响应体
# 用法: do_get URL [TOKEN]  ->  设置 $HTTP_CODE $BODY
do_request() {
  local method="$1"
  local url="$2"
  local token="$3"
  local data="$4"
  local content_type="${5:-application/json}"

  local auth_header=""
  [[ -n "$token" ]] && auth_header="-H \"Authorization: Bearer $token\""

  local cmd="curl -s -w '\n%{http_code}' -X $method"
  cmd="$cmd -H 'Content-Type: $content_type'"
  [[ -n "$token" ]] && cmd="$cmd -H 'Authorization: Bearer $token'"
  [[ -n "$data" && "$method" != "GET" ]] && cmd="$cmd -d '$data'"
  cmd="$cmd '$url'"

  local response
  response=$(eval $cmd)
  HTTP_CODE=$(echo "$response" | tail -1)
  BODY=$(echo "$response" | sed '$d')
}

do_get() { do_request "GET" "$1" "$2"; }
do_post() { do_request "POST" "$1" "$2" "$3"; }
do_put() { do_request "PUT" "$1" "$2" "$3"; }
do_patch() { do_request "PATCH" "$1" "$2" "$3"; }
do_delete() { do_request "DELETE" "$1" "$2"; }

# 从返回 JSON 提取字段
json_val() {
  echo "$1" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d$2)" 2>/dev/null
}

# ── 检查后端是否启动 ─────────────────────────────────────────────────────────

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║         前端-后端 API 全量联调测试                          ║"
echo "║         Backend: $BASE_URL                       ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

printf "${YELLOW}⏳ 检查后端是否运行中...${NC}\n"
if ! curl -s --max-time 3 "$BASE_URL/dicts" > /dev/null 2>&1; then
  printf "${RED}❌ 后端未运行! 请先启动 Spring Boot 后端 (端口 8080)${NC}\n"
  echo "   提示: cd swim-admin && ./mvnw spring-boot:run"
  exit 1
fi
printf "${GREEN}✅ 后端已就绪${NC}\n\n"

# ============================================================================
#  1. Auth 模块 (/api/auth)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [1/12] Auth 模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 1.1 管理员登录
do_post "$BASE_URL/auth/login" "" '{"username":"admin","password":"admin123"}'
assert "POST /auth/login (admin登录)" "$HTTP_CODE" "$BODY"
ADMIN_TOKEN=$(json_val "$BODY" "['data']['token']")
if [[ -z "$ADMIN_TOKEN" || "$ADMIN_TOKEN" == "None" ]]; then
  printf "${RED}‼️  管理员登录失败, 无法获取 token, 后续测试将受影响!${NC}\n"
  ADMIN_TOKEN=""
fi

# 1.2 普通用户登录
do_post "$BASE_URL/auth/login" "" '{"username":"parent1","password":"123456"}'
assert "POST /auth/login (parent1登录)" "$HTTP_CODE" "$BODY"
USER_TOKEN=$(json_val "$BODY" "['data']['token']")
if [[ -z "$USER_TOKEN" || "$USER_TOKEN" == "None" ]]; then
  printf "${RED}‼️  用户登录失败, 无法获取 token!${NC}\n"
  USER_TOKEN=""
fi

# 1.3 获取当前用户信息
do_get "$BASE_URL/auth/me" "$ADMIN_TOKEN"
assert "GET  /auth/me (admin)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/auth/me" "$USER_TOKEN"
assert "GET  /auth/me (parent1)" "$HTTP_CODE" "$BODY"

# 1.4 注册 (使用随机用户名避免重复)
RAND_USER="testuser_$(date +%s)"
do_post "$BASE_URL/auth/register" "" "{\"username\":\"$RAND_USER\",\"password\":\"test1234\",\"displayName\":\"测试用户\"}"
assert "POST /auth/register (注册新用户)" "$HTTP_CODE" "$BODY"

# 1.5 登录失败 (错误密码)
do_post "$BASE_URL/auth/login" "" '{"username":"admin","password":"wrong"}'
assert "POST /auth/login (错误密码=401)" "$HTTP_CODE" "$BODY" "401"

echo ""

# ============================================================================
#  2. Dict 模块 (/api/dicts)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [2/12] Dict 字典模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

do_get "$BASE_URL/dicts" "$ADMIN_TOKEN"
assert "GET  /dicts (获取全部字典)" "$HTTP_CODE" "$BODY"

echo ""

# ============================================================================
#  3. AdminUser 模块 (/api/admin/users)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [3/12] AdminUser 用户管理模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 3.1 分页列表
do_get "$BASE_URL/admin/users?page=1&size=5" "$ADMIN_TOKEN"
assert "GET  /admin/users (分页列表)" "$HTTP_CODE" "$BODY"

# 3.2 带搜索关键字
do_get "$BASE_URL/admin/users?page=1&size=5&keyword=parent" "$ADMIN_TOKEN"
assert "GET  /admin/users?keyword=parent (搜索)" "$HTTP_CODE" "$BODY"

# 3.3 创建用户
CRUD_USER="cruduser_$(date +%s)"
do_post "$BASE_URL/admin/users" "$ADMIN_TOKEN" "{\"username\":\"$CRUD_USER\",\"password\":\"test1234\",\"role\":1,\"displayName\":\"CRUD测试\"}"
assert "POST /admin/users (创建用户)" "$HTTP_CODE" "$BODY"
CRUD_USER_ID=$(json_val "$BODY" "['data']['id']")

# 3.4 修改用户
if [[ -n "$CRUD_USER_ID" && "$CRUD_USER_ID" != "None" ]]; then
  do_put "$BASE_URL/admin/users/$CRUD_USER_ID" "$ADMIN_TOKEN" '{"displayName":"CRUD已修改"}'
  assert "PUT  /admin/users/$CRUD_USER_ID (修改用户)" "$HTTP_CODE" "$BODY"

  # 3.5 修改状态
  do_patch "$BASE_URL/admin/users/$CRUD_USER_ID/status" "$ADMIN_TOKEN" '{"status":2}'
  assert "PATCH /admin/users/$CRUD_USER_ID/status (禁用)" "$HTTP_CODE" "$BODY"
fi

# 3.6 权限测试 (普通用户不应访问)
do_get "$BASE_URL/admin/users?page=1&size=5" "$USER_TOKEN"
assert "GET  /admin/users (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

echo ""

# ============================================================================
#  4. Baby 宝宝档案模块 (/api/babies)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [4/12] Baby 宝宝档案模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 4.1 分页列表 (admin)
do_get "$BASE_URL/babies?page=1&size=5" "$ADMIN_TOKEN"
assert "GET  /babies (admin分页列表)" "$HTTP_CODE" "$BODY"

# 4.2 分页列表 (user)
do_get "$BASE_URL/babies?page=1&size=10" "$USER_TOKEN"
assert "GET  /babies (user分页列表)" "$HTTP_CODE" "$BODY"

# 4.3 创建
do_post "$BASE_URL/babies" "$USER_TOKEN" '{"name":"测试宝宝","gender":1,"birthday":"2024-06-01","relation":"妈妈","note":"测试数据"}'
assert "POST /babies (创建宝宝档案)" "$HTTP_CODE" "$BODY"
BABY_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$BABY_ID" && "$BABY_ID" != "None" ]]; then
  # 4.4 详情
  do_get "$BASE_URL/babies/$BABY_ID" "$USER_TOKEN"
  assert "GET  /babies/$BABY_ID (详情)" "$HTTP_CODE" "$BODY"

  # 4.5 修改
  do_put "$BASE_URL/babies/$BABY_ID" "$USER_TOKEN" '{"name":"测试宝宝已改名"}'
  assert "PUT  /babies/$BABY_ID (修改)" "$HTTP_CODE" "$BODY"

  # 4.6 删除
  do_delete "$BASE_URL/babies/$BABY_ID" "$USER_TOKEN"
  assert "DEL  /babies/$BABY_ID (删除)" "$HTTP_CODE" "$BODY"
fi

echo ""

# ============================================================================
#  5. GrowthLog 成长日志模块 (/api/growth-logs)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [5/12] GrowthLog 成长日志模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 5.1 分页列表
do_get "$BASE_URL/growth-logs?page=1&size=5" "$ADMIN_TOKEN"
assert "GET  /growth-logs (admin分页列表)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/growth-logs?page=1&size=5" "$USER_TOKEN"
assert "GET  /growth-logs (user分页列表)" "$HTTP_CODE" "$BODY"

# 5.2 带筛选
do_get "$BASE_URL/growth-logs?page=1&size=5&babyId=1&logType=1" "$USER_TOKEN"
assert "GET  /growth-logs?babyId=1&logType=1 (筛选)" "$HTTP_CODE" "$BODY"

# 5.3 创建 (babyId=1 属于 parent1=userId2, 用 USER_TOKEN 的 parent1)
do_post "$BASE_URL/growth-logs" "$USER_TOKEN" '{"babyId":1,"logType":1,"title":"测试喂养","content":"测试内容","logTime":"2026-03-14T10:00:00"}'
assert "POST /growth-logs (创建日志)" "$HTTP_CODE" "$BODY"
LOG_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$LOG_ID" && "$LOG_ID" != "None" ]]; then
  # 5.4 详情
  do_get "$BASE_URL/growth-logs/$LOG_ID" "$USER_TOKEN"
  assert "GET  /growth-logs/$LOG_ID (详情)" "$HTTP_CODE" "$BODY"

  # 5.5 修改
  do_put "$BASE_URL/growth-logs/$LOG_ID" "$USER_TOKEN" '{"title":"已修改标题"}'
  assert "PUT  /growth-logs/$LOG_ID (修改)" "$HTTP_CODE" "$BODY"

  # 5.6 删除
  do_delete "$BASE_URL/growth-logs/$LOG_ID" "$USER_TOKEN"
  assert "DEL  /growth-logs/$LOG_ID (删除)" "$HTTP_CODE" "$BODY"
fi

echo ""

# ============================================================================
#  6. GrowthMetric 成长指标模块 (/api/growth-metrics)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [6/12] GrowthMetric 成长指标模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 6.1 分页列表
do_get "$BASE_URL/growth-metrics?page=1&size=5" "$ADMIN_TOKEN"
assert "GET  /growth-metrics (admin分页列表)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/growth-metrics?page=1&size=5" "$USER_TOKEN"
assert "GET  /growth-metrics (user分页列表)" "$HTTP_CODE" "$BODY"

# 6.2 创建
do_post "$BASE_URL/growth-metrics" "$USER_TOKEN" '{"babyId":1,"metricType":2,"metricValue":9.5,"unit":"kg","recordedAt":"2026-03-14T10:00:00"}'
assert "POST /growth-metrics (创建指标)" "$HTTP_CODE" "$BODY"
METRIC_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$METRIC_ID" && "$METRIC_ID" != "None" ]]; then
  # 6.3 详情
  do_get "$BASE_URL/growth-metrics/$METRIC_ID" "$USER_TOKEN"
  assert "GET  /growth-metrics/$METRIC_ID (详情)" "$HTTP_CODE" "$BODY"

  # 6.4 修改
  do_put "$BASE_URL/growth-metrics/$METRIC_ID" "$USER_TOKEN" '{"metricValue":9.8,"note":"已修正"}'
  assert "PUT  /growth-metrics/$METRIC_ID (修改)" "$HTTP_CODE" "$BODY"

  # 6.5 删除
  do_delete "$BASE_URL/growth-metrics/$METRIC_ID" "$USER_TOKEN"
  assert "DEL  /growth-metrics/$METRIC_ID (删除)" "$HTTP_CODE" "$BODY"
fi

echo ""

# ============================================================================
#  7. Category / Tag / Topic (用户端 list)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [7/12] 用户端 Category / Tag / Topic${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

do_get "$BASE_URL/categories" "$USER_TOKEN"
assert "GET  /categories (用户端分类列表)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/tags" "$USER_TOKEN"
assert "GET  /tags (用户端标签列表)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/topics" "$USER_TOKEN"
assert "GET  /topics (用户端话题列表)" "$HTTP_CODE" "$BODY"

echo ""

# ============================================================================
#  8. Admin Category / Tag / Topic (管理端 CRUD)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [8/12] 管理端 Category / Tag / Topic CRUD${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── Category CRUD ──
do_get "$BASE_URL/admin/categories" "$ADMIN_TOKEN"
assert "GET  /admin/categories (管理端分类列表)" "$HTTP_CODE" "$BODY"

do_post "$BASE_URL/admin/categories" "$ADMIN_TOKEN" '{"name":"测试分类","description":"自动化测试","sortOrder":99}'
assert "POST /admin/categories (创建分类)" "$HTTP_CODE" "$BODY"
CAT_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$CAT_ID" && "$CAT_ID" != "None" ]]; then
  do_put "$BASE_URL/admin/categories/$CAT_ID" "$ADMIN_TOKEN" '{"name":"测试分类已改"}'
  assert "PUT  /admin/categories/$CAT_ID (修改分类)" "$HTTP_CODE" "$BODY"

  do_delete "$BASE_URL/admin/categories/$CAT_ID" "$ADMIN_TOKEN"
  assert "DEL  /admin/categories/$CAT_ID (删除分类)" "$HTTP_CODE" "$BODY"
fi

# ── Tag CRUD ──
do_get "$BASE_URL/admin/tags" "$ADMIN_TOKEN"
assert "GET  /admin/tags (管理端标签列表)" "$HTTP_CODE" "$BODY"

do_post "$BASE_URL/admin/tags" "$ADMIN_TOKEN" '{"name":"测试标签"}'
assert "POST /admin/tags (创建标签)" "$HTTP_CODE" "$BODY"
TAG_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$TAG_ID" && "$TAG_ID" != "None" ]]; then
  do_put "$BASE_URL/admin/tags/$TAG_ID" "$ADMIN_TOKEN" '{"name":"测试标签已改"}'
  assert "PUT  /admin/tags/$TAG_ID (修改标签)" "$HTTP_CODE" "$BODY"

  do_delete "$BASE_URL/admin/tags/$TAG_ID" "$ADMIN_TOKEN"
  assert "DEL  /admin/tags/$TAG_ID (删除标签)" "$HTTP_CODE" "$BODY"
fi

# ── Topic CRUD ──
do_get "$BASE_URL/admin/topics" "$ADMIN_TOKEN"
assert "GET  /admin/topics (管理端话题列表)" "$HTTP_CODE" "$BODY"

do_post "$BASE_URL/admin/topics" "$ADMIN_TOKEN" '{"name":"测试话题","description":"自动化测试"}'
assert "POST /admin/topics (创建话题)" "$HTTP_CODE" "$BODY"
TOPIC_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$TOPIC_ID" && "$TOPIC_ID" != "None" ]]; then
  do_put "$BASE_URL/admin/topics/$TOPIC_ID" "$ADMIN_TOKEN" '{"name":"测试话题已改"}'
  assert "PUT  /admin/topics/$TOPIC_ID (修改话题)" "$HTTP_CODE" "$BODY"

  do_delete "$BASE_URL/admin/topics/$TOPIC_ID" "$ADMIN_TOKEN"
  assert "DEL  /admin/topics/$TOPIC_ID (删除话题)" "$HTTP_CODE" "$BODY"
fi

echo ""

# ============================================================================
#  9. Post 帖子模块 (用户端 + 管理端)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [9/12] Post 帖子模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 9.1 用户端分页
do_get "$BASE_URL/posts?page=1&size=5" "$USER_TOKEN"
assert "GET  /posts (用户端帖子列表)" "$HTTP_CODE" "$BODY"

# 9.2 用户端带筛选
do_get "$BASE_URL/posts?page=1&size=5&categoryId=1" "$USER_TOKEN"
assert "GET  /posts?categoryId=1 (按分类筛选)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/posts?page=1&size=5&keyword=喂养" "$USER_TOKEN"
assert "GET  /posts?keyword=喂养 (关键字搜索)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/posts?page=1&size=5&mine=true" "$USER_TOKEN"
assert "GET  /posts?mine=true (我的帖子)" "$HTTP_CODE" "$BODY"

# 9.3 创建帖子
do_post "$BASE_URL/posts" "$USER_TOKEN" '{"categoryId":1,"topicId":1,"title":"联调测试帖","content":"自动化测试内容","tagIds":[1,2]}'
assert "POST /posts (创建帖子)" "$HTTP_CODE" "$BODY"
POST_ID=$(json_val "$BODY" "['data']['id']")

if [[ -n "$POST_ID" && "$POST_ID" != "None" ]]; then
  # 9.4 详情
  do_get "$BASE_URL/posts/$POST_ID" "$USER_TOKEN"
  assert "GET  /posts/$POST_ID (帖子详情)" "$HTTP_CODE" "$BODY"

  # 9.5 修改
  do_put "$BASE_URL/posts/$POST_ID" "$USER_TOKEN" '{"title":"已修改的帖子标题"}'
  assert "PUT  /posts/$POST_ID (修改帖子)" "$HTTP_CODE" "$BODY"

  # ── 管理端 ──
  # 9.6 管理端列表
  do_get "$BASE_URL/admin/posts?page=1&size=5" "$ADMIN_TOKEN"
  assert "GET  /admin/posts (管理端帖子列表)" "$HTTP_CODE" "$BODY"

  do_get "$BASE_URL/admin/posts?page=1&size=5&status=2" "$ADMIN_TOKEN"
  assert "GET  /admin/posts?status=2 (待审核)" "$HTTP_CODE" "$BODY"

  # 9.7 审核通过
  do_patch "$BASE_URL/admin/posts/$POST_ID/status" "$ADMIN_TOKEN" '{"status":1,"reason":"审核通过"}'
  assert "PATCH /admin/posts/$POST_ID/status (审核通过)" "$HTTP_CODE" "$BODY"

  # 9.8 删除帖子 (清理)
  do_delete "$BASE_URL/posts/$POST_ID" "$USER_TOKEN"
  assert "DEL  /posts/$POST_ID (删除帖子)" "$HTTP_CODE" "$BODY"
fi

echo ""

# ============================================================================
#  10. Comment 评论模块 (用户端 + 管理端)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [10/12] Comment 评论模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 10.1 查看已有帖子的评论 (postId=1 是已发布的)
do_get "$BASE_URL/posts/1/comments?page=1&size=10" "$USER_TOKEN"
assert "GET  /posts/1/comments (帖子评论列表)" "$HTTP_CODE" "$BODY"

# 10.2 创建评论
do_post "$BASE_URL/posts/1/comments" "$USER_TOKEN" '{"content":"联调测试评论"}'
assert "POST /posts/1/comments (创建评论)" "$HTTP_CODE" "$BODY"
COMMENT_ID=$(json_val "$BODY" "['data']['id']")

# 10.3 管理端评论列表
do_get "$BASE_URL/admin/comments?page=1&size=5" "$ADMIN_TOKEN"
assert "GET  /admin/comments (管理端评论列表)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/admin/comments?page=1&size=5&postId=1" "$ADMIN_TOKEN"
assert "GET  /admin/comments?postId=1 (按帖子筛选)" "$HTTP_CODE" "$BODY"

# 10.4 管理端更新评论状态
if [[ -n "$COMMENT_ID" && "$COMMENT_ID" != "None" ]]; then
  do_patch "$BASE_URL/admin/comments/$COMMENT_ID/status" "$ADMIN_TOKEN" '{"status":2,"reason":"测试隐藏"}'
  assert "PATCH /admin/comments/$COMMENT_ID/status (隐藏评论)" "$HTTP_CODE" "$BODY"

  # 10.5 删除评论
  do_delete "$BASE_URL/comments/$COMMENT_ID" "$USER_TOKEN"
  assert "DEL  /comments/$COMMENT_ID (删除评论)" "$HTTP_CODE" "$BODY"
fi

echo ""

# ============================================================================
#  11. Report 举报模块 (用户端 + 管理端)
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [11/12] Report 举报模块${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 11.1 创建举报
do_post "$BASE_URL/reports" "$USER_TOKEN" '{"postId":1,"reason":6,"reasonDesc":"联调测试举报"}'
assert "POST /reports (创建举报-帖子)" "$HTTP_CODE" "$BODY"
REPORT_ID=$(json_val "$BODY" "['data']['id']")

# 11.2 管理端举报列表
do_get "$BASE_URL/admin/reports?page=1&size=5" "$ADMIN_TOKEN"
assert "GET  /admin/reports (管理端举报列表)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/admin/reports?page=1&size=5&status=1" "$ADMIN_TOKEN"
assert "GET  /admin/reports?status=1 (待处理)" "$HTTP_CODE" "$BODY"

# 11.3 处理举报
if [[ -n "$REPORT_ID" && "$REPORT_ID" != "None" ]]; then
  do_patch "$BASE_URL/admin/reports/$REPORT_ID/handle" "$ADMIN_TOKEN" '{"status":2,"handleResult":1,"handleNote":"联调测试-忽略"}'
  assert "PATCH /admin/reports/$REPORT_ID/handle (处理举报)" "$HTTP_CODE" "$BODY"
fi

# 11.4 错误测试: 同时选帖子和评论
do_post "$BASE_URL/reports" "$USER_TOKEN" '{"postId":1,"commentId":1,"reason":1}'
assert "POST /reports (同时选帖子+评论=400)" "$HTTP_CODE" "$BODY" "400"

echo ""

# ============================================================================
#  12. Stats 统计模块 + File 上传
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}📋 [12/12] Stats 统计 + File 上传${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 12.1 宝宝指标统计 (babyId=1)
do_get "$BASE_URL/stats/baby/1/metrics" "$USER_TOKEN"
assert "GET  /stats/baby/1/metrics (宝宝指标)" "$HTTP_CODE" "$BODY"

do_get "$BASE_URL/stats/baby/1/metrics?metricType=2" "$USER_TOKEN"
assert "GET  /stats/baby/1/metrics?metricType=2 (按类型)" "$HTTP_CODE" "$BODY"

# 12.2 社区概览
do_get "$BASE_URL/stats/community/overview?days=7" "$ADMIN_TOKEN"
assert "GET  /stats/community/overview?days=7 (社区概览)" "$HTTP_CODE" "$BODY"

# 12.3 文件上传 (创建临时文件)
TMPFILE=$(mktemp /tmp/api-test-XXXXXX.txt)
echo "联调测试文件内容" > "$TMPFILE"

UPLOAD_RESPONSE=$(curl -s -w '\n%{http_code}' -X POST \
  -H "Authorization: Bearer $USER_TOKEN" \
  -F "file=@$TMPFILE" \
  "$BASE_URL/files/upload")
UPLOAD_HTTP=$(echo "$UPLOAD_RESPONSE" | tail -1)
UPLOAD_BODY=$(echo "$UPLOAD_RESPONSE" | sed '$d')
assert "POST /files/upload (文件上传)" "$UPLOAD_HTTP" "$UPLOAD_BODY"

rm -f "$TMPFILE"

echo ""

# ============================================================================
#  跨角色安全测试
# ============================================================================
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
printf "${CYAN}🔒 跨角色安全测试${NC}\n"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# 未登录访问
do_get "$BASE_URL/auth/me" ""
assert "GET  /auth/me (未登录=401)" "$HTTP_CODE" "$BODY" "401"

# 普通用户访问管理端
do_get "$BASE_URL/admin/posts?page=1&size=5" "$USER_TOKEN"
assert "GET  /admin/posts (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

do_get "$BASE_URL/admin/comments?page=1&size=5" "$USER_TOKEN"
assert "GET  /admin/comments (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

do_get "$BASE_URL/admin/reports?page=1&size=5" "$USER_TOKEN"
assert "GET  /admin/reports (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

do_post "$BASE_URL/admin/categories" "$USER_TOKEN" '{"name":"hack"}'
assert "POST /admin/categories (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

do_post "$BASE_URL/admin/tags" "$USER_TOKEN" '{"name":"hack"}'
assert "POST /admin/tags (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

do_post "$BASE_URL/admin/topics" "$USER_TOKEN" '{"name":"hack"}'
assert "POST /admin/topics (普通用户=403)" "$HTTP_CODE" "$BODY" "403"

echo ""

# ============================================================================
#  测试报告
# ============================================================================
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║                     📊 测试报告                            ║"
echo "╠══════════════════════════════════════════════════════════════╣"
printf "║  总计: %-4d  ✅ 通过: %-4d  ❌ 失败: %-4d               ║\n" "$TOTAL" "$PASS" "$FAIL"
echo "╚══════════════════════════════════════════════════════════════╝"

if [[ ${#FAILED_APIS[@]} -gt 0 ]]; then
  echo ""
  printf "${RED}失败接口列表:${NC}\n"
  for api in "${FAILED_APIS[@]}"; do
    echo "  • $api"
  done
fi

echo ""
if [[ $FAIL -eq 0 ]]; then
  printf "${GREEN}🎉 全部测试通过! 前后端联调零问题!${NC}\n"
  exit 0
else
  printf "${RED}⚠️  存在 $FAIL 个失败接口, 请检查上方错误信息!${NC}\n"
  exit 1
fi
