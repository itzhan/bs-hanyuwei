#!/bin/bash
# ================================================================
# 育儿社区 — 前后端 API 联调测试脚本
# 用法: bash api-test.sh [BASE_URL]
# 默认: http://localhost:8080
# ================================================================

set -euo pipefail

BASE="${1:-http://localhost:8080}"
PASS=0
FAIL=0
TOTAL=0
ERRORS=()

# ---- 颜色 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ---- 工具函数 ----
check() {
  local name="$1"
  local method="$2"
  local url="$3"
  shift 3
  TOTAL=$((TOTAL + 1))

  local response status body
  if [[ "$method" == "POST" ]] || [[ "$method" == "PUT" ]] || [[ "$method" == "PATCH" ]]; then
    response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" "$@" 2>/dev/null || echo -e "\n000")
  elif [[ "$method" == "DELETE" ]]; then
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$url" "$@" 2>/dev/null || echo -e "\n000")
  else
    response=$(curl -s -w "\n%{http_code}" "$url" "$@" 2>/dev/null || echo -e "\n000")
  fi

  status=$(echo "$response" | tail -1)
  body=$(echo "$response" | sed '$d')

  if [[ "$status" -ge 200 ]] && [[ "$status" -lt 300 ]]; then
    # 检查业务 code
    local biz_code
    biz_code=$(echo "$body" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('code',''))" 2>/dev/null || echo "")
    if [[ "$biz_code" == "0" ]] || [[ -z "$biz_code" ]]; then
      echo -e "  ${GREEN}✓${NC} $name ${CYAN}[HTTP $status]${NC}"
      PASS=$((PASS + 1))
      echo "$body"  # 返回 body 供后续提取
      return 0
    else
      local msg
      msg=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('message',''))" 2>/dev/null || echo "")
      echo -e "  ${RED}✗${NC} $name ${CYAN}[HTTP $status]${NC} → code=$biz_code msg=$msg"
      FAIL=$((FAIL + 1))
      ERRORS+=("$name → code=$biz_code msg=$msg")
      return 1
    fi
  else
    echo -e "  ${RED}✗${NC} $name ${CYAN}[HTTP $status]${NC}"
    FAIL=$((FAIL + 1))
    ERRORS+=("$name → HTTP $status")
    return 1
  fi
}

extract() {
  local json="$1"
  local path="$2"
  echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print($path)" 2>/dev/null || echo ""
}

# ================================================================
echo ""
echo -e "${YELLOW}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║       育儿社区 — 前后端 API 联调测试                    ║${NC}"
echo -e "${YELLOW}║       后端地址: ${BASE}                      ║${NC}"
echo -e "${YELLOW}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

# ================================================================
# 0. 连通性检查
# ================================================================
echo -e "${CYAN}[0/8] 连通性检查${NC}"
if ! curl -s --connect-timeout 3 "$BASE/api/auth/me" >/dev/null 2>&1; then
  echo -e "  ${RED}✗ 无法连接后端 $BASE，请确保后端已启动${NC}"
  exit 1
fi
echo -e "  ${GREEN}✓${NC} 后端可达"
echo ""

# ================================================================
# 1. 认证模块
# ================================================================
echo -e "${CYAN}[1/8] 认证模块 (/api/auth)${NC}"

# 注册测试用户
TS=$(date +%s)
TEST_USER="testuser_${TS}"
TEST_PASS="Test123456"
REG_BODY=$(check "注册新用户" POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${TEST_USER}\",\"password\":\"${TEST_PASS}\",\"displayName\":\"测试用户${TS}\"}" 2>/dev/null) || true

# 登录
LOGIN_BODY=$(check "用户登录" POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"${TEST_USER}\",\"password\":\"${TEST_PASS}\"}" 2>/dev/null) || true

TOKEN=$(extract "$LOGIN_BODY" "d['data']['token']")
if [[ -z "$TOKEN" ]]; then
  echo -e "  ${RED}✗ 无法获取Token，后续测试将失败${NC}"
  TOKEN="INVALID"
fi
AUTH="-H \"Authorization: Bearer ${TOKEN}\""

# 获取当前用户
ME_BODY=$(check "获取当前用户 /me" GET "$BASE/api/auth/me" \
  -H "Authorization: Bearer ${TOKEN}" 2>/dev/null) || true
USER_ID=$(extract "$ME_BODY" "d['data']['id']")
echo ""

# ================================================================
# 2. 宝宝档案模块
# ================================================================
echo -e "${CYAN}[2/8] 宝宝档案模块 (/api/babies)${NC}"

# 创建宝宝
BABY_BODY=$(check "创建宝宝" POST "$BASE/api/babies" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{"name":"测试宝宝","gender":1,"birthday":"2024-06-15","relation":"爸爸","note":"联调测试"}' 2>/dev/null) || true
BABY_ID=$(extract "$BABY_BODY" "d['data']['id']")

# 宝宝列表
check "宝宝列表" GET "$BASE/api/babies?page=1&size=10" \
  -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true

# 宝宝详情
if [[ -n "$BABY_ID" ]]; then
  check "宝宝详情" GET "$BASE/api/babies/${BABY_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi

# 更新宝宝
if [[ -n "$BABY_ID" ]]; then
  check "更新宝宝" PUT "$BASE/api/babies/${BABY_ID}" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{"name":"测试宝宝-更新","gender":2}' >/dev/null 2>/dev/null || true
fi
echo ""

# ================================================================
# 3. 成长日志模块
# ================================================================
echo -e "${CYAN}[3/8] 成长日志模块 (/api/growth-logs)${NC}"

# 创建日志
LOG_BODY=$(check "创建成长日志" POST "$BASE/api/growth-logs" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "{\"babyId\":${BABY_ID:-1},\"logType\":1,\"title\":\"联调测试日志\",\"content\":\"记录内容\",\"logTime\":\"2025-03-15 10:00:00\"}" 2>/dev/null) || true
LOG_ID=$(extract "$LOG_BODY" "d['data']['id']")

# 日志列表
check "日志列表" GET "$BASE/api/growth-logs?page=1&size=10" \
  -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true

# 日志详情
if [[ -n "$LOG_ID" ]]; then
  check "日志详情" GET "$BASE/api/growth-logs/${LOG_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi

# 更新日志
if [[ -n "$LOG_ID" ]]; then
  check "更新日志" PUT "$BASE/api/growth-logs/${LOG_ID}" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d "{\"babyId\":${BABY_ID:-1},\"logType\":2,\"title\":\"更新测试\",\"content\":\"更新内容\",\"logTime\":\"2025-03-15 11:00:00\"}" >/dev/null 2>/dev/null || true
fi

# 删除日志
if [[ -n "$LOG_ID" ]]; then
  check "删除日志" DELETE "$BASE/api/growth-logs/${LOG_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi
echo ""

# ================================================================
# 4. 成长指标模块
# ================================================================
echo -e "${CYAN}[4/8] 成长指标模块 (/api/growth-metrics)${NC}"

# 创建指标
METRIC_BODY=$(check "创建成长指标" POST "$BASE/api/growth-metrics" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d "{\"babyId\":${BABY_ID:-1},\"metricType\":1,\"metricValue\":75.5,\"unit\":\"cm\",\"recordedAt\":\"2025-03-15 10:00:00\"}" 2>/dev/null) || true
METRIC_ID=$(extract "$METRIC_BODY" "d['data']['id']")

# 指标列表
check "指标列表" GET "$BASE/api/growth-metrics?page=1&size=10" \
  -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true

# 指标详情
if [[ -n "$METRIC_ID" ]]; then
  check "指标详情" GET "$BASE/api/growth-metrics/${METRIC_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi

# 更新指标
if [[ -n "$METRIC_ID" ]]; then
  check "更新指标" PUT "$BASE/api/growth-metrics/${METRIC_ID}" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d "{\"babyId\":${BABY_ID:-1},\"metricType\":1,\"metricValue\":76.0,\"unit\":\"cm\",\"recordedAt\":\"2025-03-15 11:00:00\"}" >/dev/null 2>/dev/null || true
fi

# 删除指标
if [[ -n "$METRIC_ID" ]]; then
  check "删除指标" DELETE "$BASE/api/growth-metrics/${METRIC_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi
echo ""

# ================================================================
# 5. 统计模块
# ================================================================
echo -e "${CYAN}[5/8] 统计模块 (/api/stats)${NC}"

# Dashboard
check "仪表盘统计" GET "$BASE/api/stats/dashboard" \
  -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true

# 社区概览
check "社区概览" GET "$BASE/api/stats/community/overview?days=30" \
  -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true

# 宝宝指标统计
if [[ -n "$BABY_ID" ]]; then
  check "宝宝指标统计" GET "$BASE/api/stats/baby/${BABY_ID}/metrics?metricType=1" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi
echo ""

# ================================================================
# 6. 社区模块
# ================================================================
echo -e "${CYAN}[6/8] 社区模块 (/api/posts + /api/categories,topics,tags)${NC}"

# 分类、话题、标签
check "获取分类" GET "$BASE/api/categories" >/dev/null 2>/dev/null || true
check "获取话题" GET "$BASE/api/topics" >/dev/null 2>/dev/null || true
check "获取标签" GET "$BASE/api/tags" >/dev/null 2>/dev/null || true

# 创建帖子
POST_BODY=$(check "发布帖子" POST "$BASE/api/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}" \
  -d '{"title":"联调测试帖子","content":"这是一篇联调测试帖子的内容","tagIds":[]}' 2>/dev/null) || true
POST_ID=$(extract "$POST_BODY" "d['data']['id']")

# 帖子列表
check "帖子列表" GET "$BASE/api/posts?page=1&size=10" >/dev/null 2>/dev/null || true

# 帖子列表(mine)
check "我的帖子" GET "$BASE/api/posts?page=1&size=10&mine=true" \
  -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true

# 帖子详情（需要管理员审核才能公开可见，用自己看自己的帖子）
if [[ -n "$POST_ID" ]]; then
  check "帖子详情" GET "$BASE/api/posts/${POST_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi

# 更新帖子
if [[ -n "$POST_ID" ]]; then
  check "更新帖子" PUT "$BASE/api/posts/${POST_ID}" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{"title":"联调测试帖子-更新","content":"更新内容"}' >/dev/null 2>/dev/null || true
fi
echo ""

# ================================================================
# 7. 评论模块
# ================================================================
echo -e "${CYAN}[7/8] 评论模块 (/api/posts/{id}/comments + /api/comments)${NC}"

# 注意: 只能对已发布(PUBLISHED)的帖子评论, 测试帖子是PENDING状态
# 尝试获取评论列表（应该对所有帖子都能查）
if [[ -n "$POST_ID" ]]; then
  check "评论列表" GET "$BASE/api/posts/${POST_ID}/comments?page=1&size=10" >/dev/null 2>/dev/null || true
fi

# 尝试对 PENDING 帖子评论（预期可能失败 403，但验证接口路径正确）
if [[ -n "$POST_ID" ]]; then
  COMMENT_BODY=$(check "发表评论(自己的待审核帖子)" POST "$BASE/api/posts/${POST_ID}/comments" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{"content":"联调测试评论"}' 2>/dev/null) || true
  COMMENT_ID=$(extract "$COMMENT_BODY" "d['data']['id']")

  # 删除评论
  if [[ -n "$COMMENT_ID" ]]; then
    check "删除评论" DELETE "$BASE/api/comments/${COMMENT_ID}" \
      -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
  fi
fi
echo ""

# ================================================================
# 8. 举报 & 字典 & 文件
# ================================================================
echo -e "${CYAN}[8/8] 举报/字典/文件模块${NC}"

# 举报
if [[ -n "$POST_ID" ]]; then
  check "提交举报" POST "$BASE/api/reports" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d "{\"postId\":${POST_ID},\"reason\":1,\"reasonDesc\":\"联调测试举报\"}" >/dev/null 2>/dev/null || true
fi

# 字典
check "获取字典" GET "$BASE/api/dicts" >/dev/null 2>/dev/null || true

# 文件上传 (创建一个小临时文件)
TMP_FILE="/tmp/api_test_upload.txt"
echo "test" > "$TMP_FILE"
check "文件上传" POST "$BASE/api/files/upload" \
  -H "Authorization: Bearer ${TOKEN}" \
  -F "file=@${TMP_FILE}" >/dev/null 2>/dev/null || true
rm -f "$TMP_FILE"
echo ""

# ================================================================
# 清理测试数据
# ================================================================
echo -e "${CYAN}[cleanup] 清理测试数据${NC}"

# 删除帖子
if [[ -n "$POST_ID" ]]; then
  check "删除测试帖子" DELETE "$BASE/api/posts/${POST_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi

# 删除宝宝
if [[ -n "$BABY_ID" ]]; then
  check "删除测试宝宝" DELETE "$BASE/api/babies/${BABY_ID}" \
    -H "Authorization: Bearer ${TOKEN}" >/dev/null 2>/dev/null || true
fi
echo ""

# ================================================================
# 结果汇总
# ================================================================
echo -e "${YELLOW}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${YELLOW}║                   测 试 结 果 汇 总                     ║${NC}"
echo -e "${YELLOW}╠══════════════════════════════════════════════════════════╣${NC}"
printf "${YELLOW}║${NC}  总计: %-5s  ${GREEN}通过: %-5s${NC}  ${RED}失败: %-5s${NC}            ${YELLOW}║${NC}\n" "$TOTAL" "$PASS" "$FAIL"
echo -e "${YELLOW}╚══════════════════════════════════════════════════════════╝${NC}"

if [[ ${#ERRORS[@]} -gt 0 ]]; then
  echo ""
  echo -e "${RED}失败详情:${NC}"
  for err in "${ERRORS[@]}"; do
    echo -e "  ${RED}•${NC} $err"
  done
fi

echo ""
if [[ $FAIL -eq 0 ]]; then
  echo -e "${GREEN}🎉 全部通过！前后端联调无问题${NC}"
  exit 0
else
  echo -e "${RED}⚠️  有 ${FAIL} 个接口需要检查${NC}"
  exit 1
fi
