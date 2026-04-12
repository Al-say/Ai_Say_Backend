# JWT 认证系统测试指南 🔐

本文档提供完整的 JWT 认证系统测试流程，包括注册、登录、Token 验证等。

---

## 📋 测试前准备

### 1. 启动应用

```bash
cd /Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend

# 设置必要的环境变量
export JWT_SECRET="YourSuperSecretKeyHereMustBeVeryLongAndComplex123!"
export DEEPSEEK_API_KEY="sk-your-key"

# 启动应用
mvn spring-boot:run
```

等待应用启动完成（端口 2580）。

---

## 🧪 完整测试流程

### Step 1: 用户注册

```bash
curl -X POST http://localhost:2580/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "email": "test@example.com",
    "displayName": "测试用户"
  }' | jq
```

**预期响应** (HTTP 200):
```json
User registered successfully!
```

**测试重复注册** (应该失败):
```bash
# 再次使用相同用户名注册
curl -X POST http://localhost:2580/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "AnotherPass",
    "displayName": "另一个用户"
  }' | jq
```

**预期响应** (HTTP 400):
```json
{
  "error": "用户名已存在"
}
```

---

### Step 2: 用户登录

```bash
curl -X POST http://localhost:2580/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }' | jq
```

**预期响应** (HTTP 200):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "user": {
    "userId": 1,
    "appleSub": null,
    "email": "test@example.com",
    "emailVerified": null,
    "displayName": "测试用户",
    "deviceId": null
  }
}
```

**📝 记下返回的 `accessToken`，用于后续测试！**

**测试错误密码**:
```bash
curl -X POST http://localhost:2580/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "WrongPassword"
  }' | jq
```

**预期响应** (HTTP 401):
```json
{
  "error": "邮箱或密码错误"
}
```

---

### Step 3: 验证 Token（获取当前用户信息）

将 `{accessToken}` 替换为实际的 Token：

```bash
curl -X GET http://localhost:2580/api/auth/me \
  -H "Authorization: Bearer {accessToken}" | jq
```

**预期响应** (HTTP 200):
```json
{
  "userId": "1",
  "appleSub": null
}
```

**测试无效 Token**:
```bash
curl -X GET http://localhost:2580/api/auth/me \
  -H "Authorization: Bearer invalid-token-here" | jq
```

**预期响应** (HTTP 401):
```json
{
  "error": "未登录"
}
```

---

### Step 5: 使用 Token 访问受保护接口

测试异步评估接口（需要认证）：

```bash
curl -X POST http://localhost:2580/api/v1/evaluate \
  -H "Authorization: Bearer {accessToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "test",
    "transcript": "I have been studying English for five years.",
    "async": true
  }' | jq
```

**预期响应** (HTTP 202):
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "progress": 0,
  "estimatedSecondsRemaining": 15
}
```

**测试不携带 Token**:
```bash
curl -X POST http://localhost:2580/api/v1/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "test",
    "transcript": "Test",
    "async": true
  }'
```

**预期响应** (HTTP 401 或 403): 拒绝访问

---

### Step 6: 查询登录历史（可选）

```bash
curl -X GET http://localhost:2580/api/profile/login-history?limit=50 \
  -H "Authorization: Bearer {accessToken}" | jq
```

**预期响应** (HTTP 200):
```json
[
  {
    "id": 101,
    "loginAt": "2026-02-05T12:34:56Z",
    "loginType": "PASSWORD",
    "deviceId": "device-uuid"
  }
]
```

---

## 🔬 数据库验证

### 查看 H2 控制台

访问：http://localhost:2580/h2-console

**连接信息**:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

**查询用户表**:
```sql
SELECT * FROM user_account;
```

**验证密码加密**:
```sql
-- 密码字段应该是 BCrypt 哈希，类似：
-- $2a$10$abcdefghijklmnopqrstuvwxyz...
SELECT email, password_hash FROM user_account;
```

---

## 🚀 自动化测试脚本

创建 `test-jwt-auth.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:2580/api/auth"
USERNAME="testuser_$(date +%s)"  # 使用时间戳生成唯一用户名
EMAIL="test-$(date +%s)@example.com"

echo "🧪 JWT 认证系统测试"
echo "===================="

# 1. 注册
echo "\n1️⃣ 测试注册..."
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"Test123456\",
    \"email\": \"$EMAIL\",
    \"displayName\": \"自动化测试用户\"
  }")

echo "注册响应: $REGISTER_RESPONSE"

# 2. 登录
echo "\n2️⃣ 测试登录..."
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"Test123456\"
  }")

echo "登录响应: $LOGIN_RESPONSE"

# 提取 Token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
echo "Token: ${TOKEN:0:20}..."

# 3. 验证 Token
echo "\n3️⃣ 测试 Token 验证..."
ME_RESPONSE=$(curl -s -X GET $BASE_URL/me \
  -H "Authorization: Bearer $TOKEN")

echo "用户信息: $ME_RESPONSE"

# 4. 测试受保护接口
echo "\n4️⃣ 测试受保护接口..."
EVAL_RESPONSE=$(curl -s -X POST http://localhost:2580/api/v1/evaluate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "test",
    "transcript": "Automated test transcript.",
    "async": true
  }')

echo "评估响应: $EVAL_RESPONSE"

echo "\n✅ 测试完成！"
```

运行测试：
```bash
chmod +x test-jwt-auth.sh
./test-jwt-auth.sh
```

---

## 🎯 性能测试

### 使用 ApacheBench

```bash
# 创建登录请求文件
cat > login.json <<EOF
{
  "username": "testuser",
  "password": "Test123456"
}
EOF

# 并发登录测试
ab -n 100 -c 10 \
  -p login.json \
  -T "application/json" \
  http://localhost:2580/api/auth/login
```

**关注指标**:
- Requests per second
- Time per request (mean)
- Failed requests (应为 0)

---

## 📊 监控与日志

### 查看应用日志

```bash
tail -f app.log | grep -E "JWT|认证|登录"
```

**关键日志**:
- `用户注册成功: email=...`
- `用户登录成功: email=...`
- `JWT 认证成功: email=...`
- `JWT 验证失败: ...`

---

## ❗ 常见问题排查

### 问题 1: Token 验证失败

**症状**: `/api/auth/me` 返回 "未登录"

**排查**:
```bash
# 检查 Token 格式
echo "Bearer eyJhbG..." | grep "^Bearer "

# 检查 JWT_SECRET 是否设置
echo $JWT_SECRET
```

### 问题 2: 密码错误但能登录

**可能原因**: PasswordEncoder 未正确配置

**排查**:
```sql
-- 检查密码哈希格式
SELECT password_hash FROM user_account LIMIT 1;
-- 应该是 $2a$10$... 开头
```

### 问题 3: 无法访问受保护接口

**症状**: 返回 403 Forbidden

**排查**:
```bash
# 检查 SecurityConfig 是否正确注册 JwtAuthenticationFilter
# 查看日志是否有 "JwtAuthenticationFilter" 相关输出
tail -100 app.log | grep JwtAuthenticationFilter
```

---

## 🔐 安全检查清单

- [ ] JWT_SECRET 使用环境变量，不硬编码
- [ ] 密码使用 BCrypt 加密
- [ ] Token 有过期时间（24 小时）
- [ ] 登录失败不暴露具体原因
- [ ] 公开接口（/api/auth/**）正确放行
- [ ] 受保护接口需要 Token 认证
- [ ] Token 在 HTTPS 下传输（生产环境）

---

**测试完成后，请更新 [快速启动指南](./快速启动指南.md)！** 🎉
