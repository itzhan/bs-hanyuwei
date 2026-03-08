@echo off
chcp 65001 >nul
title 育儿平台 - 一键启动
color 0B

echo ========================================
echo   🚀 育儿平台 一键启动 (Windows)
echo ========================================
echo.

:: ---- 获取脚本所在目录 ----
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

:: ============================================================
::  1. 清理占用端口
:: ============================================================
echo [1/3] 🧹 清理占用端口...
echo.

:: 清理 8080
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING 2^>nul') do (
    echo   ⚠️  端口 8080 被占用，正在清理 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

:: 清理 5173
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING 2^>nul') do (
    echo   ⚠️  端口 5173 被占用，正在清理 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

:: 清理 9527
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :9527 ^| findstr LISTENING 2^>nul') do (
    echo   ⚠️  端口 9527 被占用，正在清理 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
)

echo   ✅ 端口清理完成
echo.

:: ============================================================
::  2. 启动服务 (各开新窗口)
:: ============================================================
echo [2/3] 🚀 启动服务...
echo.

:: ---- 后端 (swim-admin) ----
echo   🔧 启动后端 (swim-admin)...
start "后端 - swim-admin (8080)" cmd /k "cd /d "%PROJECT_DIR%swim-admin" && color 0A && echo ====== 后端服务启动中 ====== && mvn spring-boot:run"

:: ---- 前端 (frontend) ----
echo   🌐 启动前端 (frontend)...
start "前端 - frontend (5173)" cmd /k "cd /d "%PROJECT_DIR%frontend" && color 0B && echo ====== 前端服务启动中 ====== && npm run dev"

:: ---- 后台管理 (soybean-admin-main) ----
echo   📊 启动后台管理 (soybean-admin-main)...
start "后台管理 - soybean-admin (9527)" cmd /k "cd /d "%PROJECT_DIR%soybean-admin-main" && color 0D && echo ====== 后台管理启动中 ====== && pnpm dev"

echo   ✅ 所有服务已在新窗口中启动
echo.

:: ============================================================
::  3. 打印角色账号密码
:: ============================================================
echo [3/3] 🎯 系统角色账号信息
echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║               🎯 系统角色账号信息                            ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  角色         用户名        密码         说明               ║
echo ╠══════════════════════════════════════════════════════════════╣
echo ║  系统管理员   admin         admin123     管理后台全部权限   ║
echo ║  管理员二     admin2        admin123     管理后台全部权限   ║
echo ║  家长一       parent1       123456       普通家长用户       ║
echo ║  家长二       parent2       123456       普通家长用户       ║
echo ║  家长三       parent3       123456       普通家长用户       ║
echo ║  家长四       parent4       123456       普通家长用户       ║
echo ║  家长五       parent5       123456       已禁用             ║
echo ║  家长六       parent6       123456       普通家长用户       ║
echo ║  家长七       parent7       123456       普通家长用户       ║
echo ║  家长八       parent8       123456       普通家长用户       ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo ========================================
echo   📡 服务地址
echo ========================================
echo   后端 API:    http://localhost:8080
echo   用户前端:    http://localhost:5173
echo   管理后台:    http://localhost:9527
echo ========================================
echo.
echo 💡 关闭此窗口不会影响已启动的服务
echo 💡 如需停止服务，请关闭对应的命令行窗口
echo.
pause
