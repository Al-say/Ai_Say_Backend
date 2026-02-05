# API 测试示例

本文档提供完整的 API 测试命令，用于验证异步评估流程。

---

## 📋 前置准备

### 1. 设置环境变量

```bash
# macOS/Linux
export DEEPSEEK_API_KEY=sk-your-actual-key-here

# Windows PowerShell
$env:DEEPSEEK_API_KEY="sk-your-actual-key-here"
```

### 2. 启动应用

```bash
cd /Users/alsay_mac/Synchronization/Github_File/Ai_Say_Backend
mvn spring-boot:run
```

等待看到以下日志：
```
Started SpeakingApplication in X.XXX seconds
```

---

## 🧪 完整测试流程

### Step 1: 健康检查

```bash
curl http://localhost:2580/api/v1/evaluate/health
```

**预期响应**:
```
Evaluation Service is running
```

---

### Step 2: 提交评估任务

```bash
curl -X POST http://localhost:2580/api/v1/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "job_interview",
    "transcript": "I have been working as a software engineer for five years. During this time, I have developed strong skills in Java and Spring Boot. I enjoy solving complex problems and working with team members.",
    "userId": "test-user-001",
    "async": true
  }' | jq
```

**预期响应** (HTTP 202):
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "progress": 0,
  "result": null,
  "errorMessage": null,
  "createdAt": "2026-02-05T23:30:00",
  "completedAt": null,
  "estimatedSecondsRemaining": 15
}
```

**📝 记下返回的 `taskId`，用于下一步查询！**

---

### Step 3: 轮询任务状态

将 `{taskId}` 替换为实际返回的值：

```bash
# 第一次查询（约 2 秒后）
curl http://localhost:2580/api/v1/evaluate/{taskId} | jq

# 如果状态还是 PROCESSING，再等 2-3 秒后查询
curl http://localhost:2580/api/v1/evaluate/{taskId} | jq
```

**预期响应（处理中）** (HTTP 202):
```json
{
  "taskId": "550e8400-...",
  "status": "PROCESSING",
  "progress": 60,
  "estimatedSecondsRemaining": 6
}
```

**预期响应（已完成）** (HTTP 200):
```json
{
  "taskId": "550e8400-...",
  "status": "COMPLETED",
  "progress": 100,
  "result": {
    "status": "ok",
    "overallScore": 82,
    "metrics": {
      "fluency": 85,
      "completeness": 80,
      "relevance": 88,
      "pronunciation": 78,
      "grammar": 84,
      "vocabulary": 79
    },
    "feedback": {
      "summary": "Good overall performance with clear communication.",
      "strengths": [
        "Clear structure and logical flow",
        "Professional vocabulary usage"
      ],
      "issues": [
        {
          "type": "grammar",
          "evidence": "I have been working",
          "fix": "Consider using simpler past tense for more direct communication"
        }
      ],
      "suggestions": [
        "Add specific project examples",
        "Mention quantifiable achievements"
      ],
      "improvedVersion": "I worked as a software engineer for five years, specializing in Java and Spring Boot..."
    }
  },
  "completedAt": "2026-02-05T23:30:15"
}
```

---

### Step 4: 清理任务

```bash
curl -X DELETE http://localhost:2580/api/v1/evaluate/{taskId}
```

**预期响应** (HTTP 204): 无内容

---

## 🔐 可选：登录历史查询

前提：先通过 `/api/auth/login` 或 `/api/auth/apple` 获取 `accessToken`。

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

## 🔬 测试不同场景

### 场景 1: CAREER_GROWTH 画像

```bash
curl -X POST http://localhost:2580/api/v1/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "CAREER_GROWTH",
    "scene": "business_presentation",
    "transcript": "Good morning everyone. Today I would like to present our quarterly results. As you can see from the chart, revenue increased by 25 percent compared to last quarter.",
    "async": true
  }' | jq
```

### 场景 2: 短文本（触发 invalid_input）

```bash
curl -X POST http://localhost:2580/api/v1/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "daily_practice",
    "transcript": "Hello world.",
    "async": true
  }' | jq
```

**预期**: AI 返回 `status: "invalid_input"`，分数全为 0。

### 场景 3: 非英文文本

```bash
curl -X POST http://localhost:2580/api/v1/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "exam_simulation",
    "transcript": "你好，我叫张三。今天天气很好。",
    "async": true
  }' | jq
```

**预期**: AI 返回 `status: "invalid_input"`。

---

## 🚀 并发测试

### 使用 ApacheBench

创建请求文件 `request.json`:
```json
{
  "persona": "EXAM_PREP",
  "scene": "daily_practice",
  "transcript": "I have been studying English for five years and I really enjoy it. I think English is a very useful language.",
  "async": true
}
```

执行压力测试：
```bash
ab -n 100 -c 10 \
  -p request.json \
  -T "application/json" \
  http://localhost:2580/api/v1/evaluate
```

**参数说明**:
- `-n 100`: 总请求数
- `-c 10`: 并发数
- `-p`: POST 数据文件
- `-T`: Content-Type

**关注指标**:
- Requests per second
- Time per request (mean)
- Failed requests

---

## 📊 监控与调试

### 查看应用日志

```bash
tail -f app.log | grep "taskId"
```

### 监控线程池状态

访问 Spring Boot Actuator（如果已启用）：
```bash
curl http://localhost:2580/actuator/metrics/executor.active
```

### 检查 H2 数据库

访问 H2 控制台：
```
http://localhost:2580/h2-console
```

**连接信息**:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

---

## ❗ 常见问题排查

### 问题 1: 环境变量未生效

**症状**: 日志显示 `API Key: dummy-key`

**解决**:
```bash
# 检查环境变量
echo $DEEPSEEK_API_KEY

# 确保在同一终端启动应用
export DEEPSEEK_API_KEY=sk-xxx
mvn spring-boot:run
```

### 问题 2: 任务一直 PROCESSING

**可能原因**:
1. DeepSeek API 限流
2. 网络超时
3. API Key 无效

**排查**:
```bash
# 查看详细日志
tail -100 app.log | grep ERROR
```

### 问题 3: 编译错误

```bash
# 清理并重新编译
mvn clean compile
```

---

## 🎯 自动化测试脚本

创建 `test-api.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:2580/api/v1/evaluate"

echo "🧪 开始 API 测试..."

# 1. 健康检查
echo "\n1️⃣ 健康检查..."
curl -s $BASE_URL/health
echo ""

# 2. 提交任务
echo "\n2️⃣ 提交评估任务..."
RESPONSE=$(curl -s -X POST $BASE_URL \
  -H "Content-Type: application/json" \
  -d '{
    "persona": "EXAM_PREP",
    "scene": "test",
    "transcript": "This is a test transcript for evaluation.",
    "async": true
  }')

TASK_ID=$(echo $RESPONSE | jq -r '.taskId')
echo "TaskID: $TASK_ID"

# 3. 轮询状态
echo "\n3️⃣ 轮询任务状态..."
for i in {1..10}; do
  sleep 2
  STATUS=$(curl -s $BASE_URL/$TASK_ID | jq -r '.status')
  echo "[$i/10] 状态: $STATUS"
  
  if [ "$STATUS" == "COMPLETED" ] || [ "$STATUS" == "FAILED" ]; then
    break
  fi
done

# 4. 获取结果
echo "\n4️⃣ 获取最终结果..."
curl -s $BASE_URL/$TASK_ID | jq

# 5. 清理
echo "\n5️⃣ 清理任务..."
curl -s -X DELETE $BASE_URL/$TASK_ID

echo "\n✅ 测试完成！"
```

运行：
```bash
chmod +x test-api.sh
./test-api.sh
```

---

**文档维护**: System Team  
**最后更新**: 2026-02-05
