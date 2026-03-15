#!/usr/bin/env python3
# ============================================================================
#  前端-后端 API 全量联调测试脚本
#  覆盖所有 22 个 Controller 的全部接口
#  用法: python3 api-test.py
#  前提: 后端已在 localhost:8080 启动, 数据库已导入 seed.sql
# ============================================================================

import json
import sys
import time
import os
import tempfile
import urllib.request
import urllib.error
import urllib.parse

BASE_URL = "http://localhost:8080/api"
PASS_COUNT = 0
FAIL_COUNT = 0
TOTAL_COUNT = 0
FAILED_APIS = []

# ANSI 颜色
RED = "\033[0;31m"
GREEN = "\033[0;32m"
YELLOW = "\033[1;33m"
CYAN = "\033[0;36m"
NC = "\033[0m"

# ── 工具函数 ──────────────────────────────────────────────────────────────────

def http_request(method, url, token=None, data=None, content_type="application/json"):
    """发送 HTTP 请求，返回 (http_code, body_dict)"""
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    body_bytes = None
    if data is not None and method != "GET":
        if isinstance(data, dict):
            body_bytes = json.dumps(data).encode("utf-8")
            headers["Content-Type"] = content_type
        elif isinstance(data, bytes):
            body_bytes = data
        else:
            body_bytes = str(data).encode("utf-8")
            headers["Content-Type"] = content_type

    req = urllib.request.Request(url, data=body_bytes, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        try:
            return resp.status, json.loads(body)
        except json.JSONDecodeError:
            return resp.status, {"_raw": body}
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        try:
            return e.code, json.loads(body)
        except json.JSONDecodeError:
            return e.code, {"_raw": body, "code": e.code}
    except Exception as e:
        return 0, {"_raw": str(e), "code": -1}


def multipart_upload(url, file_path, token=None, fields=None):
    """文件上传 - multipart/form-data"""
    import mimetypes
    boundary = f"----PythonBoundary{int(time.time())}"

    body_parts = []
    # 额外字段
    if fields:
        for key, value in fields.items():
            body_parts.append(f"--{boundary}\r\n".encode())
            body_parts.append(f'Content-Disposition: form-data; name="{key}"\r\n\r\n'.encode())
            body_parts.append(f"{value}\r\n".encode())

    # 文件
    filename = os.path.basename(file_path)
    mime_type = mimetypes.guess_type(filename)[0] or "application/octet-stream"
    body_parts.append(f"--{boundary}\r\n".encode())
    body_parts.append(f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode())
    body_parts.append(f"Content-Type: {mime_type}\r\n\r\n".encode())
    with open(file_path, "rb") as f:
        body_parts.append(f.read())
    body_parts.append(b"\r\n")
    body_parts.append(f"--{boundary}--\r\n".encode())

    body_bytes = b"".join(body_parts)
    headers = {"Content-Type": f"multipart/form-data; boundary={boundary}"}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    req = urllib.request.Request(url, data=body_bytes, headers=headers, method="POST")
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        body = resp.read().decode("utf-8")
        return resp.status, json.loads(body)
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="replace")
        try:
            return e.code, json.loads(body)
        except json.JSONDecodeError:
            return e.code, {"_raw": body, "code": e.code}
    except Exception as e:
        return 0, {"_raw": str(e), "code": -1}


def assert_test(name, http_code, body, expect_biz_code=0):
    """断言 HTTP 状态码 + 业务 code"""
    global PASS_COUNT, FAIL_COUNT, TOTAL_COUNT
    TOTAL_COUNT += 1

    biz_code = body.get("code", "?") if isinstance(body, dict) else "?"

    if http_code == 200 and biz_code == expect_biz_code:
        PASS_COUNT += 1
        print(f"{GREEN}  ✅ {name:<55} HTTP={http_code}  code={biz_code}{NC}")
    else:
        FAIL_COUNT += 1
        FAILED_APIS.append(f"{name} (HTTP={http_code}, code={biz_code})")
        print(f"{RED}  ❌ {name:<55} HTTP={http_code}  code={biz_code}{NC}")
        detail = json.dumps(body, ensure_ascii=False)[:250] if isinstance(body, dict) else str(body)[:250]
        print(f"     └─ {detail}")


def section(num, total, title):
    print("━" * 62)
    print(f"{CYAN}📋 [{num}/{total}] {title}{NC}")
    print("━" * 62)


def json_get(body, *keys):
    """安全地从嵌套 dict 中取值"""
    result = body
    for k in keys:
        if isinstance(result, dict):
            result = result.get(k)
        else:
            return None
    return result


# ── 主流程 ────────────────────────────────────────────────────────────────────

def main():
    global PASS_COUNT, FAIL_COUNT, TOTAL_COUNT

    print()
    print("╔══════════════════════════════════════════════════════════════╗")
    print("║         前端-后端 API 全量联调测试                          ║")
    print(f"║         Backend: {BASE_URL:<41} ║")
    print("╚══════════════════════════════════════════════════════════════╝")
    print()

    # ── 检查后端 ──
    print(f"{YELLOW}⏳ 检查后端是否运行中...{NC}")
    try:
        http_request("GET", f"{BASE_URL}/auth/login")
    except Exception:
        pass
    code, body = http_request("POST", f"{BASE_URL}/auth/login",
                               data={"username": "admin", "password": "admin123"})
    if code != 200 or body.get("code") != 0:
        print(f"{RED}❌ 后端未运行或登录失败! 请先启动 Spring Boot 后端 (端口 8080){NC}")
        print(f"   提示: cd swim-admin && ./mvnw spring-boot:run")
        print(f"   响应: HTTP={code}, body={body}")
        sys.exit(1)
    print(f"{GREEN}✅ 后端已就绪{NC}")
    print()

    # ========================================================================
    # 1. Auth 模块
    # ========================================================================
    section(1, 12, "Auth 认证模块")

    # 1.1 管理员登录
    code, body = http_request("POST", f"{BASE_URL}/auth/login",
                               data={"username": "admin", "password": "admin123"})
    assert_test("POST /auth/login (admin登录)", code, body)
    ADMIN_TOKEN = json_get(body, "data", "token")
    if not ADMIN_TOKEN:
        print(f"{RED}‼️  管理员 Token 获取失败, 后续测试将受影响!{NC}")
        ADMIN_TOKEN = ""

    # 1.2 普通用户登录
    code, body = http_request("POST", f"{BASE_URL}/auth/login",
                               data={"username": "parent1", "password": "123456"})
    if code == 200 and body.get("code") == 0:
        assert_test("POST /auth/login (parent1登录)", code, body)
        USER_TOKEN = json_get(body, "data", "token")
    else:
        # parent1 密码可能不匹配, 先注册一个新用户
        print(f"{YELLOW}  ⚠️  parent1 登录失败 (可能密码不匹配), 注册新用户作为替代...{NC}")
        rand_user = f"testparent_{int(time.time())}"
        code2, body2 = http_request("POST", f"{BASE_URL}/auth/register",
                                     data={"username": rand_user, "password": "test1234",
                                           "displayName": "测试家长"})
        assert_test(f"POST /auth/register (注册 {rand_user})", code2, body2)
        # 登录新用户
        code3, body3 = http_request("POST", f"{BASE_URL}/auth/login",
                                     data={"username": rand_user, "password": "test1234"})
        assert_test("POST /auth/login (新用户登录)", code3, body3)
        USER_TOKEN = json_get(body3, "data", "token")

    if not USER_TOKEN:
        print(f"{RED}‼️  用户 Token 获取失败!{NC}")
        USER_TOKEN = ""

    # 1.3 获取当前用户信息
    code, body = http_request("GET", f"{BASE_URL}/auth/me", token=ADMIN_TOKEN)
    assert_test("GET  /auth/me (admin)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/auth/me", token=USER_TOKEN)
    assert_test("GET  /auth/me (user)", code, body)

    # 1.4 注册
    rand_user2 = f"testuser_{int(time.time())}"
    code, body = http_request("POST", f"{BASE_URL}/auth/register",
                               data={"username": rand_user2, "password": "test1234",
                                     "displayName": "注册测试"})
    assert_test("POST /auth/register (注册新用户)", code, body)

    # 1.5 登录失败
    code, body = http_request("POST", f"{BASE_URL}/auth/login",
                               data={"username": "admin", "password": "wrong"})
    assert_test("POST /auth/login (错误密码=401)", code, body, expect_biz_code=401)

    print()

    # ========================================================================
    # 2. Dict 模块
    # ========================================================================
    section(2, 12, "Dict 字典模块")

    code, body = http_request("GET", f"{BASE_URL}/dicts", token=ADMIN_TOKEN)
    assert_test("GET  /dicts (获取全部字典)", code, body)

    print()

    # ========================================================================
    # 3. AdminUser 用户管理
    # ========================================================================
    section(3, 12, "AdminUser 用户管理模块")

    code, body = http_request("GET", f"{BASE_URL}/admin/users?page=1&size=5", token=ADMIN_TOKEN)
    assert_test("GET  /admin/users (分页列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/admin/users?page=1&size=5&keyword=parent", token=ADMIN_TOKEN)
    assert_test("GET  /admin/users?keyword=parent (搜索)", code, body)

    # 创建
    crud_user = f"cruduser_{int(time.time())}"
    code, body = http_request("POST", f"{BASE_URL}/admin/users", token=ADMIN_TOKEN,
                               data={"username": crud_user, "password": "test1234", "role": 1,
                                     "displayName": "CRUD测试"})
    assert_test("POST /admin/users (创建用户)", code, body)
    crud_user_id = json_get(body, "data", "id")

    if crud_user_id:
        code, body = http_request("PUT", f"{BASE_URL}/admin/users/{crud_user_id}", token=ADMIN_TOKEN,
                                   data={"displayName": "CRUD已修改"})
        assert_test(f"PUT  /admin/users/{crud_user_id} (修改用户)", code, body)

        code, body = http_request("PATCH", f"{BASE_URL}/admin/users/{crud_user_id}/status", token=ADMIN_TOKEN,
                                   data={"status": 2})
        assert_test(f"PATCH /admin/users/{crud_user_id}/status (禁用)", code, body)

    # 权限测试
    code, body = http_request("GET", f"{BASE_URL}/admin/users?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /admin/users (普通用户=403)", code, body, expect_biz_code=403)

    print()

    # ========================================================================
    # 4. Baby 宝宝档案
    # ========================================================================
    section(4, 12, "Baby 宝宝档案模块")

    code, body = http_request("GET", f"{BASE_URL}/babies?page=1&size=5", token=ADMIN_TOKEN)
    assert_test("GET  /babies (admin分页列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/babies?page=1&size=10", token=USER_TOKEN)
    assert_test("GET  /babies (user分页列表)", code, body)

    # 创建 - 使用 USER_TOKEN
    code, body = http_request("POST", f"{BASE_URL}/babies", token=USER_TOKEN,
                               data={"name": "测试宝宝", "gender": 1, "birthday": "2024-06-01",
                                     "relation": "妈妈", "note": "测试数据"})
    assert_test("POST /babies (创建宝宝档案)", code, body)
    baby_id = json_get(body, "data", "id")

    if baby_id:
        code, body = http_request("GET", f"{BASE_URL}/babies/{baby_id}", token=USER_TOKEN)
        assert_test(f"GET  /babies/{baby_id} (详情)", code, body)

        code, body = http_request("PUT", f"{BASE_URL}/babies/{baby_id}", token=USER_TOKEN,
                                   data={"name": "测试宝宝已改名"})
        assert_test(f"PUT  /babies/{baby_id} (修改)", code, body)

        code, body = http_request("DELETE", f"{BASE_URL}/babies/{baby_id}", token=USER_TOKEN)
        assert_test(f"DEL  /babies/{baby_id} (删除)", code, body)

    print()

    # ========================================================================
    # 5. GrowthLog 成长日志
    # ========================================================================
    section(5, 12, "GrowthLog 成长日志模块")

    code, body = http_request("GET", f"{BASE_URL}/growth-logs?page=1&size=5", token=ADMIN_TOKEN)
    assert_test("GET  /growth-logs (admin分页列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/growth-logs?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /growth-logs (user分页列表)", code, body)

    # 先创建宝宝 (确保有属于当前用户的 baby)
    code_b, body_b = http_request("POST", f"{BASE_URL}/babies", token=USER_TOKEN,
                                   data={"name": "日志测试宝宝", "gender": 2})
    test_baby_id = json_get(body_b, "data", "id")

    if test_baby_id:
        code, body = http_request("POST", f"{BASE_URL}/growth-logs", token=USER_TOKEN,
                                   data={"babyId": test_baby_id, "logType": 1, "title": "测试喂养",
                                         "content": "测试内容", "logTime": "2026-03-14T10:00:00"})
        assert_test("POST /growth-logs (创建日志)", code, body)
        log_id = json_get(body, "data", "id")

        if log_id:
            code, body = http_request("GET", f"{BASE_URL}/growth-logs/{log_id}", token=USER_TOKEN)
            assert_test(f"GET  /growth-logs/{log_id} (详情)", code, body)

            code, body = http_request("PUT", f"{BASE_URL}/growth-logs/{log_id}", token=USER_TOKEN,
                                       data={"title": "已修改标题"})
            assert_test(f"PUT  /growth-logs/{log_id} (修改)", code, body)

            code, body = http_request("DELETE", f"{BASE_URL}/growth-logs/{log_id}", token=USER_TOKEN)
            assert_test(f"DEL  /growth-logs/{log_id} (删除)", code, body)

    print()

    # ========================================================================
    # 6. GrowthMetric 成长指标
    # ========================================================================
    section(6, 12, "GrowthMetric 成长指标模块")

    code, body = http_request("GET", f"{BASE_URL}/growth-metrics?page=1&size=5", token=ADMIN_TOKEN)
    assert_test("GET  /growth-metrics (admin分页列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/growth-metrics?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /growth-metrics (user分页列表)", code, body)

    if test_baby_id:
        code, body = http_request("POST", f"{BASE_URL}/growth-metrics", token=USER_TOKEN,
                                   data={"babyId": test_baby_id, "metricType": 2, "metricValue": 9.5,
                                         "unit": "kg", "recordedAt": "2026-03-14T10:00:00"})
        assert_test("POST /growth-metrics (创建指标)", code, body)
        metric_id = json_get(body, "data", "id")

        if metric_id:
            code, body = http_request("GET", f"{BASE_URL}/growth-metrics/{metric_id}", token=USER_TOKEN)
            assert_test(f"GET  /growth-metrics/{metric_id} (详情)", code, body)

            code, body = http_request("PUT", f"{BASE_URL}/growth-metrics/{metric_id}", token=USER_TOKEN,
                                       data={"metricValue": 9.8, "note": "已修正"})
            assert_test(f"PUT  /growth-metrics/{metric_id} (修改)", code, body)

            code, body = http_request("DELETE", f"{BASE_URL}/growth-metrics/{metric_id}", token=USER_TOKEN)
            assert_test(f"DEL  /growth-metrics/{metric_id} (删除)", code, body)

    # 清理测试宝宝
    if test_baby_id:
        http_request("DELETE", f"{BASE_URL}/babies/{test_baby_id}", token=USER_TOKEN)

    print()

    # ========================================================================
    # 7. Category / Tag / Topic (用户端)
    # ========================================================================
    section(7, 12, "用户端 Category / Tag / Topic")

    code, body = http_request("GET", f"{BASE_URL}/categories", token=USER_TOKEN)
    assert_test("GET  /categories (用户端分类列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/tags", token=USER_TOKEN)
    assert_test("GET  /tags (用户端标签列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/topics", token=USER_TOKEN)
    assert_test("GET  /topics (用户端话题列表)", code, body)

    print()

    # ========================================================================
    # 8. Admin Category / Tag / Topic CRUD
    # ========================================================================
    section(8, 12, "管理端 Category / Tag / Topic CRUD")

    # Category
    code, body = http_request("GET", f"{BASE_URL}/admin/categories", token=ADMIN_TOKEN)
    assert_test("GET  /admin/categories (管理端分类列表)", code, body)

    code, body = http_request("POST", f"{BASE_URL}/admin/categories", token=ADMIN_TOKEN,
                               data={"name": "测试分类", "description": "自动化测试", "sortOrder": 99})
    assert_test("POST /admin/categories (创建分类)", code, body)
    cat_id = json_get(body, "data", "id")
    if cat_id:
        code, body = http_request("PUT", f"{BASE_URL}/admin/categories/{cat_id}", token=ADMIN_TOKEN,
                                   data={"name": "测试分类已改"})
        assert_test(f"PUT  /admin/categories/{cat_id} (修改分类)", code, body)
        code, body = http_request("DELETE", f"{BASE_URL}/admin/categories/{cat_id}", token=ADMIN_TOKEN)
        assert_test(f"DEL  /admin/categories/{cat_id} (删除分类)", code, body)

    # Tag
    code, body = http_request("GET", f"{BASE_URL}/admin/tags", token=ADMIN_TOKEN)
    assert_test("GET  /admin/tags (管理端标签列表)", code, body)

    code, body = http_request("POST", f"{BASE_URL}/admin/tags", token=ADMIN_TOKEN,
                               data={"name": "测试标签"})
    assert_test("POST /admin/tags (创建标签)", code, body)
    tag_id = json_get(body, "data", "id")
    if tag_id:
        code, body = http_request("PUT", f"{BASE_URL}/admin/tags/{tag_id}", token=ADMIN_TOKEN,
                                   data={"name": "测试标签已改"})
        assert_test(f"PUT  /admin/tags/{tag_id} (修改标签)", code, body)
        code, body = http_request("DELETE", f"{BASE_URL}/admin/tags/{tag_id}", token=ADMIN_TOKEN)
        assert_test(f"DEL  /admin/tags/{tag_id} (删除标签)", code, body)

    # Topic
    code, body = http_request("GET", f"{BASE_URL}/admin/topics", token=ADMIN_TOKEN)
    assert_test("GET  /admin/topics (管理端话题列表)", code, body)

    code, body = http_request("POST", f"{BASE_URL}/admin/topics", token=ADMIN_TOKEN,
                               data={"name": "测试话题", "description": "自动化测试"})
    assert_test("POST /admin/topics (创建话题)", code, body)
    topic_id = json_get(body, "data", "id")
    if topic_id:
        code, body = http_request("PUT", f"{BASE_URL}/admin/topics/{topic_id}", token=ADMIN_TOKEN,
                                   data={"name": "测试话题已改"})
        assert_test(f"PUT  /admin/topics/{topic_id} (修改话题)", code, body)
        code, body = http_request("DELETE", f"{BASE_URL}/admin/topics/{topic_id}", token=ADMIN_TOKEN)
        assert_test(f"DEL  /admin/topics/{topic_id} (删除话题)", code, body)

    print()

    # ========================================================================
    # 9. Post 帖子模块
    # ========================================================================
    section(9, 12, "Post 帖子模块")

    # 用户端列表
    code, body = http_request("GET", f"{BASE_URL}/posts?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /posts (用户端帖子列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/posts?page=1&size=5&categoryId=1", token=USER_TOKEN)
    assert_test("GET  /posts?categoryId=1 (按分类筛选)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/posts?page=1&size=5&mine=true", token=USER_TOKEN)
    assert_test("GET  /posts?mine=true (我的帖子)", code, body)

    # 创建帖子
    code, body = http_request("POST", f"{BASE_URL}/posts", token=USER_TOKEN,
                               data={"categoryId": 1, "topicId": 1, "title": "联调测试帖",
                                     "content": "自动化测试内容", "tagIds": [1, 2]})
    assert_test("POST /posts (创建帖子)", code, body)
    post_id = json_get(body, "data", "id")

    if post_id:
        code, body = http_request("GET", f"{BASE_URL}/posts/{post_id}", token=USER_TOKEN)
        assert_test(f"GET  /posts/{post_id} (帖子详情)", code, body)

        code, body = http_request("PUT", f"{BASE_URL}/posts/{post_id}", token=USER_TOKEN,
                                   data={"title": "已修改的帖子标题"})
        assert_test(f"PUT  /posts/{post_id} (修改帖子)", code, body)

        # 管理端
        code, body = http_request("GET", f"{BASE_URL}/admin/posts?page=1&size=5", token=ADMIN_TOKEN)
        assert_test("GET  /admin/posts (管理端帖子列表)", code, body)

        code, body = http_request("GET", f"{BASE_URL}/admin/posts?page=1&size=5&status=2", token=ADMIN_TOKEN)
        assert_test("GET  /admin/posts?status=2 (待审核帖子)", code, body)

        code, body = http_request("PATCH", f"{BASE_URL}/admin/posts/{post_id}/status", token=ADMIN_TOKEN,
                                   data={"status": 1, "reason": "审核通过"})
        assert_test(f"PATCH /admin/posts/{post_id}/status (审核通过)", code, body)

        # 删除帖子 (清理)
        code, body = http_request("DELETE", f"{BASE_URL}/posts/{post_id}", token=USER_TOKEN)
        assert_test(f"DEL  /posts/{post_id} (删除帖子)", code, body)

    print()

    # ========================================================================
    # 10. Comment 评论模块
    # ========================================================================
    section(10, 12, "Comment 评论模块")

    # 查看帖子评论 (postId=1, 已发布帖子)
    code, body = http_request("GET", f"{BASE_URL}/posts/1/comments?page=1&size=10", token=USER_TOKEN)
    assert_test("GET  /posts/1/comments (帖子评论列表)", code, body)

    # 创建评论
    code, body = http_request("POST", f"{BASE_URL}/posts/1/comments", token=USER_TOKEN,
                               data={"content": "联调测试评论"})
    assert_test("POST /posts/1/comments (创建评论)", code, body)
    comment_id = json_get(body, "data", "id")

    # 管理端
    code, body = http_request("GET", f"{BASE_URL}/admin/comments?page=1&size=5", token=ADMIN_TOKEN)
    assert_test("GET  /admin/comments (管理端评论列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/admin/comments?page=1&size=5&postId=1", token=ADMIN_TOKEN)
    assert_test("GET  /admin/comments?postId=1 (按帖子筛选)", code, body)

    if comment_id:
        code, body = http_request("PATCH", f"{BASE_URL}/admin/comments/{comment_id}/status", token=ADMIN_TOKEN,
                                   data={"status": 2, "reason": "测试隐藏"})
        assert_test(f"PATCH /admin/comments/{comment_id}/status (隐藏)", code, body)

        code, body = http_request("DELETE", f"{BASE_URL}/comments/{comment_id}", token=USER_TOKEN)
        assert_test(f"DEL  /comments/{comment_id} (删除评论)", code, body)

    print()

    # ========================================================================
    # 11. Report 举报模块
    # ========================================================================
    section(11, 12, "Report 举报模块")

    # 创建举报
    code, body = http_request("POST", f"{BASE_URL}/reports", token=USER_TOKEN,
                               data={"postId": 1, "reason": 6, "reasonDesc": "联调测试举报"})
    assert_test("POST /reports (创建举报-帖子)", code, body)
    report_id = json_get(body, "data", "id")

    # 管理端
    code, body = http_request("GET", f"{BASE_URL}/admin/reports?page=1&size=5", token=ADMIN_TOKEN)
    assert_test("GET  /admin/reports (管理端举报列表)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/admin/reports?page=1&size=5&status=1", token=ADMIN_TOKEN)
    assert_test("GET  /admin/reports?status=1 (待处理举报)", code, body)

    if report_id:
        code, body = http_request("PATCH", f"{BASE_URL}/admin/reports/{report_id}/handle", token=ADMIN_TOKEN,
                                   data={"status": 2, "handleResult": 1, "handleNote": "测试-忽略"})
        assert_test(f"PATCH /admin/reports/{report_id}/handle (处理举报)", code, body)

    # 错误测试
    code, body = http_request("POST", f"{BASE_URL}/reports", token=USER_TOKEN,
                               data={"postId": 1, "commentId": 1, "reason": 1})
    assert_test("POST /reports (同时选帖子+评论=400)", code, body, expect_biz_code=400)

    print()

    # ========================================================================
    # 12. Stats + File Upload
    # ========================================================================
    section(12, 12, "Stats 统计 + File 上传")

    # 宝宝指标 - 用 admin token 访问 babyId=1
    code, body = http_request("GET", f"{BASE_URL}/stats/baby/1/metrics", token=ADMIN_TOKEN)
    assert_test("GET  /stats/baby/1/metrics (宝宝指标)", code, body)

    code, body = http_request("GET", f"{BASE_URL}/stats/baby/1/metrics?metricType=2", token=ADMIN_TOKEN)
    assert_test("GET  /stats/baby/1/metrics?type=2 (按类型)", code, body)

    # 社区概览
    code, body = http_request("GET", f"{BASE_URL}/stats/community/overview?days=7", token=ADMIN_TOKEN)
    assert_test("GET  /stats/community/overview (社区概览)", code, body)

    # 文件上传
    tmp = tempfile.NamedTemporaryFile(suffix=".txt", delete=False)
    tmp.write("联调测试文件内容".encode("utf-8"))
    tmp.close()

    code, body = multipart_upload(f"{BASE_URL}/files/upload", tmp.name, token=USER_TOKEN)
    assert_test("POST /files/upload (文件上传)", code, body)

    os.unlink(tmp.name)

    print()

    # ========================================================================
    # 跨角色安全测试
    # ========================================================================
    print("━" * 62)
    print(f"{CYAN}🔒 跨角色安全测试{NC}")
    print("━" * 62)

    # 未登录
    code, body = http_request("GET", f"{BASE_URL}/auth/me")
    assert_test("GET  /auth/me (未登录=401)", code, body, expect_biz_code=401)

    # 普通用户访问管理端
    code, body = http_request("GET", f"{BASE_URL}/admin/posts?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /admin/posts (普通用户=403)", code, body, expect_biz_code=403)

    code, body = http_request("GET", f"{BASE_URL}/admin/comments?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /admin/comments (普通用户=403)", code, body, expect_biz_code=403)

    code, body = http_request("GET", f"{BASE_URL}/admin/reports?page=1&size=5", token=USER_TOKEN)
    assert_test("GET  /admin/reports (普通用户=403)", code, body, expect_biz_code=403)

    code, body = http_request("POST", f"{BASE_URL}/admin/categories", token=USER_TOKEN,
                               data={"name": "hack"})
    assert_test("POST /admin/categories (普通用户=403)", code, body, expect_biz_code=403)

    code, body = http_request("POST", f"{BASE_URL}/admin/tags", token=USER_TOKEN,
                               data={"name": "hack"})
    assert_test("POST /admin/tags (普通用户=403)", code, body, expect_biz_code=403)

    code, body = http_request("POST", f"{BASE_URL}/admin/topics", token=USER_TOKEN,
                               data={"name": "hack"})
    assert_test("POST /admin/topics (普通用户=403)", code, body, expect_biz_code=403)

    print()

    # ========================================================================
    # 测试报告
    # ========================================================================
    print("╔══════════════════════════════════════════════════════════════╗")
    print("║                     📊 测试报告                            ║")
    print("╠══════════════════════════════════════════════════════════════╣")
    print(f"║  总计: {TOTAL_COUNT:<4}  ✅ 通过: {PASS_COUNT:<4}  ❌ 失败: {FAIL_COUNT:<4}               ║")
    print("╚══════════════════════════════════════════════════════════════╝")

    if FAILED_APIS:
        print()
        print(f"{RED}失败接口列表:{NC}")
        for api in FAILED_APIS:
            print(f"  • {api}")

    print()
    if FAIL_COUNT == 0:
        print(f"{GREEN}🎉 全部测试通过! 前后端联调零问题!{NC}")
        sys.exit(0)
    else:
        print(f"{RED}⚠️  存在 {FAIL_COUNT} 个失败接口, 请检查上方错误信息!{NC}")
        sys.exit(1)


if __name__ == "__main__":
    main()
