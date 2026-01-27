# AI自然语言评估应用后端

基于Spring Boot 3.2.0 + Java 21的AI英语口语练习应用后端服务。

## 功能特性

### 🎯 核心功能

- **智能语音识别**: 集成百度ASR实现精准语音转文字
- **AI智能评价**: DeepSeek大模型提供个性化口语评价 (支持6维雷达图评分)
- **用户画像系统**: 支持备考党(EXAM_PREP)和职场人(CAREER_GROWTH)两种用户类型
- **每日挑战**: 数据库缓存AI生成的话题，提升性能和稳定性
- **音频处理**: FFmpeg转码支持多格式音频输入
- **对象存储**: MinIO (S3兼容) 存储音频文件
- **弹性容错**: Resilience4j 实现重试/熔断机制

### 📱 API模块

- **主页模块** (`/api/home`): 每日挑战题目获取
- **评估模块** (`/api/eval`): 文本评估、音频评估、完整音频评估流程
- **成长模块** (`/api/growth`): 学习进度追踪、雷达图分析、历史记录
- **探索模块** (`/api/explore`): 新功能发现
- **个人中心** (`/api/profile`): 用户信息管理、打卡统计
- **音频模块** (`/api/audio`): 音频上传与转码

### 🔧 技术栈

| 层次               | 技术                                             |
| ------------------ | ------------------------------------------------ |
| **框架**     | Spring Boot 3.2.0, Spring WebFlux                |
| **语言**     | Java 21 (虚拟线程支持)                           |
| **数据库**   | PostgreSQL + JPA/Hibernate                       |
| **AI服务**   | DeepSeek API (智能评分), 百度 ASR API (语音识别) |
| **对象存储** | MinIO (S3兼容)                                   |
| **音频处理** | FFmpeg                                           |
| **弹性容错** | Resilience4j (重试/熔断)                         |
| **构建工具** | Maven                                            |
| **其他**     | Lombok, FastJSON2, OkHttp, Jackson               |

### 🎯 AI评分维度 (6维雷达图)

| 维度              | 说明                           |
| ----------------- | ------------------------------ |
| `fluency`       | 流利度 - 语速、停顿、流畅性    |
| `completeness`  | 完整度 - 是否充分回答问题      |
| `relevance`     | 相关性 - 与场景/任务的相关程度 |
| `pronunciation` | 发音 - 清晰度和可理解性        |
| `grammar`       | 语法 - 语法准确性和复杂度      |
| `vocabulary`    | 词汇 - 词汇范围和恰当性        |

### 👤 用户画像系统

| 画像   | 英文标识          | 描述          | 评估风格                           |
| ------ | ----------------- | ------------- | ---------------------------------- |
| 备考党 | `EXAM_PREP`     | 雅思/托福考生 | 严格学术风，注重复杂语法和学术词汇 |
| 职场人 | `CAREER_GROWTH` | 商务英语用户  | 务实简洁风，注重清晰表达和商务习语 |

## 快速开始

### 环境要求

- Java 21+
- Maven 3.6+
- PostgreSQL 12+
- FFmpeg (音频转码)
- MinIO 或 S3兼容存储 (可选，用于音频存储)

### 配置环境变量

```bash
# 数据库配置
export DB_PASSWORD=your_db_password

# DeepSeek API配置
export DEEPSEEK_API_KEY=your_deepseek_api_key
export DEEPSEEK_BASE_URL=https://api.deepseek.com  # 可选
export DEEPSEEK_API_MODEL=deepseek-chat  # 可选

# 百度ASR配置
export BAIDU_APP_ID=your_baidu_app_id
export BAIDU_API_KEY=your_baidu_api_key
export BAIDU_SECRET_KEY=your_baidu_secret_key
```

### 启动依赖服务

```bash
# 启动 MinIO (对象存储)
docker-compose up -d
```

### 运行应用

```bash
mvn clean install
mvn spring-boot:run
```

应用将在 `http://localhost:8082` 启动。

## API文档

### 获取每日挑战

```
GET /api/home/daily?persona=EXAM_PREP
```

**参数:**

- `persona`: 用户画像 (`EXAM_PREP` 或 `CAREER_GROWTH`)

**响应示例:**

```json
{
  "topicDate": "2026-01-26",
  "persona": "EXAM_PREP",
  "title": "Describe Your Favorite Hobby",
  "prompt": "Talk about a hobby you enjoy and explain why it is important to you.",
  "imageUrl": "scene_daily_morning",
  "payload": {"source": "ai_generated"}
}
```

### 文本评估

```
POST /api/eval/text
Authorization: Bearer <token>
Content-Type: application/json

{
  "prompt": "Describe your favorite hobby",
  "userText": "I really enjoy playing basketball..."
}
```

### 完整音频评估 (核心接口)

```
POST /api/eval/audio/full
Authorization: Bearer <token>
Content-Type: multipart/form-data

参数:
- persona: 用户画像 (EXAM_PREP/CAREER_GROWTH)
- scene: 场景名称
- audio: 音频文件 (支持 m4a, wav, mp3 等)
```

### Apple 登录与鉴权

```
POST /api/auth/apple
Content-Type: application/json

{
  "idToken": "apple_identity_token",
  "deviceId": "device-uuid",
  "displayName": "Your Name"
}
```

响应包含 `accessToken`，后续接口通过 `Authorization: Bearer <token>` 访问。

```
GET /api/auth/me
Authorization: Bearer <token>
```

（可选）如果旧用户未绑定设备，可调用：

```
POST /api/auth/bind-device
Authorization: Bearer <token>
Content-Type: application/json

{
  "deviceId": "device-uuid"
}
```

**响应示例:**

```json
{
  "recordId": 123,
  "audioUrl": "http://localhost:9000/ai-say-audio/audio/2026-01-26/device-uuid/file.wav",
  "durationMs": 15000,
  "transcript": "I really enjoy playing basketball because...",
  "overallScore": 75,
  "fluency": 80,
  "completeness": 70,
  "relevance": 75,
  "feedback": "Good fluency with minor grammatical errors.",
  "suggestions": ["Try using more complex sentence structures", "Expand vocabulary range"]
}
```

### 获取成长历史

```
GET /api/growth/history?persona=EXAM_PREP&limit=50&from=2026-01-01T00:00:00Z
Authorization: Bearer <token>
```

### 获取成长历史（分页，推荐）

```
GET /api/growth/history/page?persona=EXAM_PREP&page=0&size=50&from=2026-01-01T00:00:00Z
Authorization: Bearer <token>
```

### 获取雷达图分析

```
GET /api/growth/analysis?persona=EXAM_PREP&from=2026-01-01T00:00:00Z
Authorization: Bearer <token>
```

### 获取成长详情（全量）

```
GET /api/growth/detail/{id}
Authorization: Bearer <token>
```

### 获取成长详情（轻量，推荐）

```
GET /api/growth/detail/{id}/lite
Authorization: Bearer <token>
```

### 获取用户统计

```
GET /api/profile/stats
Authorization: Bearer <token>
```

**响应示例:**

```json
{
  "deviceId": "device-uuid",
  "streakDays": 7,
  "lastActiveDate": "2026-01-26",
  "totalAttempts": 42,
  "totalDurationMs": 3600000
}
```

### 音频上传

```
POST /api/audio/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

参数: file (音频文件)
```

## 数据库设计

### 核心表结构

| 表名                  | 说明                             |
| --------------------- | -------------------------------- |
| `device`            | 设备身份表 (deviceId唯一标识)    |
| `assessment_record` | 评估记录流水表 (含JSONB扩展字段) |
| `daily_topics`      | 每日挑战题目缓存                 |
| `user_progress`     | 用户打卡进度统计                 |

### 表字段详情

#### device (设备表)

```sql
CREATE TABLE device (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    meta JSONB NOT NULL DEFAULT '{}'
);
```

#### assessment_record (评估记录表)

```sql
CREATE TABLE assessment_record (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL REFERENCES device(device_id),
    mode VARCHAR(16) NOT NULL,        -- TEXT/AUDIO
    persona VARCHAR(32) NOT NULL,     -- EXAM_PREP/CAREER_GROWTH
    scene VARCHAR(32) NOT NULL,
    prompt TEXT,
    overall_score NUMERIC(5,2),
    fluency NUMERIC(5,2),
    completeness NUMERIC(5,2),
    relevance NUMERIC(5,2),
    metrics JSONB NOT NULL DEFAULT '{}',   -- 扩展评分维度
    feedback JSONB NOT NULL DEFAULT '{}',  -- AI反馈详情
    audio_url TEXT,
    transcript TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);
```

#### user_progress (用户进度表)

```sql
CREATE TABLE user_progress (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(64) NOT NULL UNIQUE,
    total_attempts INTEGER NOT NULL,
    total_duration_ms BIGINT NOT NULL,
    last_active_date DATE,
    streak_days INTEGER NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE
);
```

### 定时任务

- **凌晨2点**: 清理7天前的旧题目
- **凌晨3点**: 预生成明天的挑战题目

## 核心流程

### 🔄 完整音频评估流程

```
1. 上传音频文件
       ↓
2. 设备活跃更新 (upsert device)
       ↓
3. FFmpeg 转码 → 16kHz Mono WAV
       ↓
4. 计算音频时长
       ↓
5. 上传到 MinIO/S3
       ↓
6. 百度 ASR 语音转文字
       ↓
7. DeepSeek AI 智能评分 (带重试/熔断)
       ↓
8. 持久化评估记录 (AssessmentRecord)
       ↓
9. 更新用户打卡进度 (UserProgress)
       ↓
10. 返回评估结果
```

### 🎯 每日挑战获取流程

```
1. 请求每日挑战
       ↓
2. 查询数据库缓存 (by date + persona)
       ↓
   [缓存命中] → 直接返回
       ↓
   [缓存未命中]
       ↓
3. 调用 DeepSeek 生成题目
       ↓
   [生成成功] → 保存并返回
       ↓
   [生成失败] → 使用静态兜底题目
```

## 架构特点

### 🏗️ 生产就绪

- 全局异常处理 (`GlobalExceptionHandler`)
- 虚拟线程支持高并发 (`spring.threads.virtual.enabled=true`)
- 请求追踪 (`RequestIdFilter` + MDC)
- 强类型配置管理
- 数据库连接池优化

### 🚀 性能优化

- AI生成内容数据库缓存
- 减少实时API调用
- 自动清理过期数据
- Projection 查询优化 (轻量级历史列表)

### 🔒 安全性

- 环境变量敏感信息管理
- API密钥安全存储
- 请求参数验证
- 设备ID校验防越权查询

### 🛡️ 弹性容错 (Resilience4j)

```properties
# DeepSeek API 重试配置
resilience4j.retry.instances.deepseek.max-attempts=2
resilience4j.retry.instances.deepseek.wait-duration=200ms
resilience4j.retry.instances.deepseek.enable-exponential-backoff=true

# 熔断器配置
resilience4j.circuitbreaker.instances.deepseek.sliding-window-size=20
resilience4j.circuitbreaker.instances.deepseek.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.deepseek.wait-duration-in-open-state=30s
```

## 开发指南

### 项目结构

```
src/main/java/com/zhupinzan/speaking/
├── config/                    # 配置类
│   ├── BaiduConfig.java           # 百度ASR配置
│   ├── DeepSeekClientConfig.java  # DeepSeek WebClient配置
│   ├── S3Config.java              # MinIO/S3配置
│   ├── RequestIdFilter.java       # 请求追踪过滤器
│   └── WebConfig.java             # Web配置
├── controller/                # REST控制器
│   ├── EvalController.java        # 评估接口 (/api/eval)
│   ├── HomeController.java        # 每日挑战 (/api/home)
│   ├── GrowthController.java      # 成长记录 (/api/growth)
│   ├── ProfileController.java     # 用户中心 (/api/profile)
│   ├── AudioController.java       # 音频上传 (/api/audio)
│   └── GlobalExceptionHandler.java # 全局异常处理
├── model/
│   ├── entity/                # JPA 实体
│   │   ├── AssessmentRecord.java  # 评估记录
│   │   ├── DailyTopic.java        # 每日挑战题目
│   │   ├── Device.java            # 设备
│   │   └── UserProgress.java      # 用户进度
│   ├── dto/                   # 数据传输对象
│   │   ├── DeepSeekEvalResult.java # AI评估结果
│   │   ├── EvalDTO.java           # 评估请求/响应
│   │   └── EvalAudioResp.java     # 音频评估响应
│   ├── UserPersona.java           # 用户画像枚举
│   ├── AssessmentMode.java        # 评估模式枚举
│   └── ErrorCode.java             # 错误码枚举
├── repository/                # 数据访问层
│   ├── AssessmentRecordRepository.java
│   ├── DailyTopicRepository.java
│   ├── DeviceRepository.java
│   └── UserProgressRepository.java
├── service/
│   ├── EvalOrchestratorService.java   # 评估编排 (核心流程)
│   ├── DeepSeekEvalService.java       # AI评分服务 (WebClient)
│   ├── DeepSeekService.java           # AI对话服务 (OkHttp)
│   ├── BaiduAsrService.java           # 语音转文字服务
│   ├── DailyChallengeService.java     # 每日挑战服务
│   ├── ProfileProgressService.java    # 用户进度服务
│   ├── audio/                     # 音频处理
│   │   ├── AudioTranscodeService.java # FFmpeg转码
│   │   └── AudioMetaService.java      # 音频元数据
│   ├── storage/                   # 存储服务
│   │   ├── ObjectStorageService.java  # S3上传
│   │   └── LocalStorageService.java   # 本地存储
│   ├── core/
│   │   └── PromptFactory.java     # AI Prompt 工厂
│   └── business/
│       ├── TopicGeneratorTask.java # 定时任务(题目生成)
│       └── SceneService.java       # 场景服务
└── SpeakingApplication.java       # 主启动类
```

### 添加新功能

1. 在相应模块的Controller中添加端点
2. 实现业务逻辑到Service层
3. 添加数据模型和Repository (如需要)
4. 编写单元测试
5. 更新API文档

### 配置说明

#### application.properties 主要配置

```properties
# 服务器配置
server.port=8082

# 数据库配置
spring.datasource.url=jdbc:postgresql://localhost:5432/speaking_db
spring.datasource.username=your_username
spring.datasource.password=${DB_PASSWORD:}

# DeepSeek API 配置
deepseek.api-key=${DEEPSEEK_API_KEY:}
deepseek.base-url=${DEEPSEEK_BASE_URL:https://api.deepseek.com}
deepseek.api-model=${DEEPSEEK_API_MODEL:deepseek-chat}

# 百度 ASR 配置
baidu.app-id=${BAIDU_APP_ID:}
baidu.api-key=${BAIDU_API_KEY:}
baidu.secret-key=${BAIDU_SECRET_KEY:}

# MinIO/S3 存储配置
storage.provider=minio
storage.bucket=ai-say-audio
storage.public-base-url=http://localhost:9000/ai-say-audio
storage.s3.endpoint=http://localhost:9000
storage.s3.region=us-east-1
storage.s3.access-key=minioadmin
storage.s3.secret-key=minioadmin

# FFmpeg 配置
ffmpeg.path=ffmpeg
audio.target.sample-rate=16000
audio.target.channels=1

# 虚拟线程
spring.threads.virtual.enabled=true
```

## 部署说明

### Docker Compose (本地开发)

```yaml
# docker-compose.yml
services:
  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
```

```bash
# 启动 MinIO
docker-compose up -d

# 访问 MinIO 控制台: http://localhost:9001
# 创建 bucket: ai-say-audio
```

### Docker部署 (生产环境)

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 系统服务

```bash
# 创建服务文件
sudo vim /etc/systemd/system/ai-say-backend.service

# 服务内容示例
[Unit]
Description=AI Say Backend
After=network.target

[Service]
User=appuser
WorkingDirectory=/opt/ai-say-backend
ExecStart=/usr/bin/java -jar app.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

## 监控和维护

### 健康检查

```
GET /actuator/health
```

### 应用指标

```
GET /actuator/metrics
```

### 日志查看

应用使用SLF4J + Logback进行日志记录，默认输出到控制台和文件。

## 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
