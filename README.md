# AI说话应用后端

基于Spring Boot 3.2.0 + Java 21的AI英语口语练习应用后端服务。

## 功能特性

### 🎯 核心功能
- **智能语音识别**: 集成百度ASR实现精准语音转文字
- **AI智能评价**: DeepSeek大模型提供个性化口语评价
- **用户画像系统**: 支持备考党和职场人两种用户类型
- **每日挑战**: 数据库缓存AI生成的话题，提升性能和稳定性

### 📱 API模块
- **主页模块** (`/api/home`): 每日挑战题目获取
- **成长模块** (`/api/growth`): 学习进度追踪
- **探索模块** (`/api/explore`): 新功能发现
- **个人中心** (`/api/profile`): 用户信息管理

### 🔧 技术栈
- **框架**: Spring Boot 3.2.0
- **语言**: Java 21 (虚拟线程支持)
- **数据库**: PostgreSQL + JPA/Hibernate
- **AI服务**: DeepSeek API, 百度ASR API
- **构建工具**: Maven
- **其他**: Lombok, FastJSON2, OkHttp

## 快速开始

### 环境要求
- Java 21+
- Maven 3.6+
- PostgreSQL 12+

### 配置环境变量
```bash
# 数据库配置
export DB_PASSWORD=your_db_password

# DeepSeek API配置
export DEEPSEEK_API_KEY=your_deepseek_api_key

# 百度ASR配置
export BAIDU_APP_ID=your_baidu_app_id
export BAIDU_API_KEY=your_baidu_api_key
export BAIDU_SECRET_KEY=your_baidu_secret_key
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
GET /api/home/daily-challenge?persona=EXAM_PREP
```

**参数:**
- `persona`: 用户画像 (`EXAM_PREP` 或 `CAREER_GROWTH`)

**响应示例:**
```json
{
  "id": 1,
  "title": "Describe Your Favorite Hobby",
  "description": "Talk about a hobby you enjoy...",
  "targetPersona": "EXAM_PREP",
  "forDate": "2024-01-16"
}
```

### 语音评价
```
POST /api/eval
Content-Type: multipart/form-data

参数: audioFile (语音文件), persona (用户画像)
```

## 数据库设计

### 核心表结构
- `assessment_records`: 评价记录
- `daily_topics`: 每日挑战题目缓存

### 定时任务
- **凌晨2点**: 清理7天前的旧题目
- **凌晨3点**: 预生成明天的挑战题目

## 架构特点

### 🏗️ 生产就绪
- 全局异常处理
- 虚拟线程支持高并发
- 强类型配置管理
- 数据库连接池优化

### 🚀 性能优化
- AI生成内容数据库缓存
- 减少实时API调用
- 自动清理过期数据

### 🔒 安全性
- 环境变量敏感信息管理
- API密钥安全存储
- 请求参数验证

## 开发指南

### 项目结构
```
src/main/java/com/zhupinzan/speaking/
├── config/          # 配置类
├── controller/      # REST控制器
├── model/           # 数据模型
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
│   ├── business/    # 业务服务 (定时任务等)
│   └── impl/        # 服务实现
└── SpeakingApplication.java
```

### 添加新功能
1. 在相应模块的Controller中添加端点
2. 实现业务逻辑到Service层
3. 添加数据模型和Repository (如需要)
4. 编写单元测试
5. 更新API文档

## 部署说明

### Docker部署 (推荐)
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