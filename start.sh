#!/bin/bash
# ============================================================
#  一键启动脚本 (macOS)
#  项目：育儿平台 parenting_platform
#  功能：自动导入数据库 → 启动后端 + 前端 + 后台管理 → 打印角色 → 实时日志
# ============================================================

# 注意：不使用 set -e，避免因非致命错误导致脚本退出

# ---- 颜色定义 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# ---- 项目根目录 ----
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# ---- 配置 ----
DB_NAME="parenting_platform"
DB_USER="root"
DB_PASS="ab123168"
BACKEND_PORT=8080
FRONTEND_PORT=5173
ADMIN_PORT=9527

# ---- 存储子进程 PID ----
PIDS=()

# ---- 优雅退出 ----
cleanup() {
    echo ""
    echo -e "${YELLOW}🛑 正在停止所有服务...${NC}"
    for pid in "${PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            kill -TERM "$pid" 2>/dev/null
            wait "$pid" 2>/dev/null
        fi
    done
    # 补杀残留进程
    lsof -ti :$BACKEND_PORT  | xargs kill -9 2>/dev/null || true
    lsof -ti :$FRONTEND_PORT | xargs kill -9 2>/dev/null || true
    lsof -ti :$ADMIN_PORT    | xargs kill -9 2>/dev/null || true
    echo -e "${GREEN}✅ 所有服务已停止${NC}"
    exit 0
}
trap cleanup SIGINT SIGTERM

# ============================================================
#  1. 依赖检查
# ============================================================
echo -e "${BOLD}${BLUE}========================================${NC}"
echo -e "${BOLD}${BLUE}  🔍 检查运行环境依赖${NC}"
echo -e "${BOLD}${BLUE}========================================${NC}"

MISSING=0
for cmd in java mvn node npm mysql; do
    if command -v $cmd &>/dev/null; then
        VERSION=$($cmd --version 2>&1 | head -n1)
        echo -e "  ${GREEN}✅ $cmd${NC} — $VERSION"
    else
        echo -e "  ${RED}❌ $cmd 未安装${NC}"
        MISSING=1
    fi
done

# pnpm 单独检查
if command -v pnpm &>/dev/null; then
    VERSION=$(pnpm --version 2>&1 | head -n1)
    echo -e "  ${GREEN}✅ pnpm${NC} — $VERSION"
else
    echo -e "  ${RED}❌ pnpm 未安装${NC}"
    MISSING=1
fi

if [ $MISSING -eq 1 ]; then
    echo -e "${RED}⚠️  请先安装缺少的依赖后再运行此脚本${NC}"
    exit 1
fi
echo ""

# ============================================================
#  2. 数据库智能导入
# ============================================================
echo -e "${BOLD}${BLUE}========================================${NC}"
echo -e "${BOLD}${BLUE}  🗄️  数据库初始化检查${NC}"
echo -e "${BOLD}${BLUE}========================================${NC}"

# 检查 MySQL 是否运行
if ! mysql -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1" &>/dev/null; then
    echo -e "${YELLOW}⚠️  MySQL 服务未启动，尝试启动...${NC}"
    if command -v brew &>/dev/null; then
        brew services start mysql 2>/dev/null || true
        sleep 3
    fi
    if ! mysql -u "$DB_USER" -p"$DB_PASS" -e "SELECT 1" &>/dev/null; then
        echo -e "${RED}❌ 无法连接 MySQL，请确保 MySQL 已安装并运行${NC}"
        exit 1
    fi
fi

# 检查数据库是否存在
DB_EXISTS=$(mysql -u "$DB_USER" -p"$DB_PASS" -N -e "SHOW DATABASES LIKE '$DB_NAME';" 2>/dev/null)

if [ -z "$DB_EXISTS" ]; then
    echo -e "  ${CYAN}📦 数据库 $DB_NAME 不存在，开始导入...${NC}"

    echo -e "  ${CYAN}   → 导入 schema.sql (建表)...${NC}"
    mysql -u "$DB_USER" -p"$DB_PASS" < "$PROJECT_DIR/db/schema.sql"

    echo -e "  ${CYAN}   → 导入 seed.sql (种子数据)...${NC}"
    mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$PROJECT_DIR/db/seed.sql"

    echo -e "  ${GREEN}✅ 数据库导入完成！${NC}"
else
    echo -e "  ${GREEN}⏭️  数据库 $DB_NAME 已存在，跳过导入${NC}"
fi
echo ""

# ============================================================
#  3. 清理占用端口
# ============================================================
echo -e "${BOLD}${BLUE}========================================${NC}"
echo -e "${BOLD}${BLUE}  🧹 清理占用端口${NC}"
echo -e "${BOLD}${BLUE}========================================${NC}"

for PORT in $BACKEND_PORT $FRONTEND_PORT $ADMIN_PORT; do
    PID_ON_PORT=$(lsof -ti :$PORT 2>/dev/null || true)
    if [ -n "$PID_ON_PORT" ]; then
        PID_LIST=$(echo "$PID_ON_PORT" | tr '\n' ',' | sed 's/,$//')
        echo -e "  ${YELLOW}⚠️  端口 $PORT 被占用，正在清理 (PID: $PID_LIST)${NC}"
        echo "$PID_ON_PORT" | xargs kill -9 2>/dev/null || true
        sleep 1
    else
        echo -e "  ${GREEN}✅ 端口 $PORT 空闲${NC}"
    fi
done
echo ""

# ============================================================
#  4. 启动服务
# ============================================================
echo -e "${BOLD}${BLUE}========================================${NC}"
echo -e "${BOLD}${BLUE}  🚀 启动服务${NC}"
echo -e "${BOLD}${BLUE}========================================${NC}"

# 创建日志目录
mkdir -p "$PROJECT_DIR/.logs"

# ---- 4a. 启动后端 (swim-admin) ----
echo -e "  ${CYAN}🔧 启动后端 (swim-admin) ...${NC}"
(cd "$PROJECT_DIR/swim-admin" && mvn spring-boot:run -q 2>&1 | tee "$PROJECT_DIR/.logs/backend.log") &
BACKEND_PID=$!
PIDS+=($BACKEND_PID)
echo -e "  ${GREEN}   PID: $BACKEND_PID${NC}"

# ---- 4b. 启动前端 (frontend) ----
echo -e "  ${CYAN}🌐 启动前端 (frontend) ...${NC}"
(cd "$PROJECT_DIR/frontend" && npm run dev 2>&1 | tee "$PROJECT_DIR/.logs/frontend.log") &
FRONTEND_PID=$!
PIDS+=($FRONTEND_PID)
echo -e "  ${GREEN}   PID: $FRONTEND_PID${NC}"

# ---- 4c. 启动后台管理 (soybean-admin-main) ----
echo -e "  ${CYAN}📊 启动后台管理 (soybean-admin-main) ...${NC}"
(cd "$PROJECT_DIR/soybean-admin-main" && pnpm dev 2>&1 | tee "$PROJECT_DIR/.logs/admin.log") &
ADMIN_PID=$!
PIDS+=($ADMIN_PID)
echo -e "  ${GREEN}   PID: $ADMIN_PID${NC}"

echo ""

# ============================================================
#  5. 打印角色账号密码
# ============================================================
echo -e "${BOLD}${MAGENTA}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}${MAGENTA}║               🎯 系统角色账号信息                            ║${NC}"
echo -e "${BOLD}${MAGENTA}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${BOLD}${MAGENTA}║  角色         用户名        密码         说明               ║${NC}"
echo -e "${BOLD}${MAGENTA}╠══════════════════════════════════════════════════════════════╣${NC}"
echo -e "${MAGENTA}║  ${YELLOW}系统管理员${MAGENTA}   admin         admin123     管理后台全部权限   ║${NC}"
echo -e "${MAGENTA}║  ${YELLOW}管理员二${MAGENTA}     admin2        admin123     管理后台全部权限   ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长一${MAGENTA}       parent1       123456       普通家长用户       ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长二${MAGENTA}       parent2       123456       普通家长用户       ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长三${MAGENTA}       parent3       123456       普通家长用户       ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长四${MAGENTA}       parent4       123456       普通家长用户       ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长五${MAGENTA}       parent5       123456       已禁用             ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长六${MAGENTA}       parent6       123456       普通家长用户       ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长七${MAGENTA}       parent7       123456       普通家长用户       ║${NC}"
echo -e "${MAGENTA}║  ${CYAN}家长八${MAGENTA}       parent8       123456       普通家长用户       ║${NC}"
echo -e "${BOLD}${MAGENTA}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${BOLD}${GREEN}========================================${NC}"
echo -e "${BOLD}${GREEN}  📡 服务地址${NC}"
echo -e "${BOLD}${GREEN}========================================${NC}"
echo -e "  ${CYAN}后端 API:${NC}    http://localhost:$BACKEND_PORT"
echo -e "  ${CYAN}用户前端:${NC}    http://localhost:$FRONTEND_PORT"
echo -e "  ${CYAN}管理后台:${NC}    http://localhost:$ADMIN_PORT"
echo -e "${BOLD}${GREEN}========================================${NC}"
echo ""
echo -e "${YELLOW}💡 按 Ctrl+C 停止所有服务${NC}"
echo ""
echo -e "${BOLD}${BLUE}========== 📋 实时日志 ==========${NC}"

# ============================================================
#  6. 实时日志 — 等待所有子进程
# ============================================================
wait
