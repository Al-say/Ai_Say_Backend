# 🚨 故障排除指南

## 端口占用问题 (Port Already in Use)

### 问题描述
```
Web server failed to start. Port 2580 was already in use.
```

### 根本原因
之前的Spring Boot进程没有完全终止，导致端口被残留进程占用。

### 解决方案

#### 方案一：清理进程（推荐）
```bash
# macOS/Linux
lsof -i :2580                    # 查看占用进程
kill -9 <PID>                    # 杀掉进程
mvn spring-boot:run             # 重新启动
```

#### 方案二：更换端口
```bash
# 临时更换端口
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
```

#### 方案三：修改配置
在 `application.yml` 中添加：
```yaml
server:
  port: 8080
```

### 预防措施
- 使用 `Ctrl+C` 停止应用，不要直接关闭终端
- 启动前检查端口：`lsof -i :2580`

---

## 数据库连接问题

### PostgreSQL 连接失败
```
org.postgresql.util.PSQLException: Connection refused
```

**检查项**：
- PostgreSQL 服务是否启动：`brew services list`
- 数据库是否存在：`psql -l`
- 连接参数是否正确

**解决方案**：
```bash
# 启动 PostgreSQL
brew services start postgresql

# 创建数据库
createdb speaking_db

# 或者使用 Docker
docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=your_password postgres:13
```

---

## 前端跨域问题 (CORS)

### 问题现象
```
Access to XMLHttpRequest at 'http://localhost:2580/api/auth/login' from origin 'http://localhost:3000' has been blocked by CORS policy
```

### 根本原因
浏览器阻止前端应用跨域访问后端API

### 解决方案

**方案一：配置后端CORS（推荐）**
已在 `SecurityConfig.java` 中配置：
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:*",      // 本地开发
        "http://127.0.0.1:*",     // 本地开发
        "http://192.168.*.*:*",   // 内网IP
        "http://10.*.*.*:*"       // 内网IP
    ));
    // ... 其他配置
}
```

**方案二：前端代理（开发环境）**
在前端项目中配置代理：
```javascript
// vue.config.js 或 next.config.js
devServer: {
  proxy: {
    '/api': {
      target: 'http://localhost:2580',
      changeOrigin: true
    }
  }
}
```

**方案三：浏览器扩展（临时）**
安装浏览器CORS扩展允许跨域请求（仅用于开发测试）

---

## 环境变量问题

### DEEPSEEK_API_KEY 未设置
```
DeepSeek API call failed: API key is required
```

**解决方案**：
```bash
export DEEPSEEK_API_KEY="sk-your-real-key"
mvn spring-boot:run
```

---

## 编译问题

### Java 版本不匹配
```
java.lang.UnsupportedClassVersionError
```

**检查 Java 版本**：
```bash
java -version  # 应为 Java 21
mvn -v         # Maven 版本
```

---

## 其他常见问题

### 内存不足
```
java.lang.OutOfMemoryError: Java heap space
```

**解决方案**：
在 `application.yml` 中调整 JVM 参数：
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
```

### 依赖冲突
```
java.lang.NoClassDefFoundError
```

**解决方案**：
```bash
mvn dependency:tree -Dverbose  # 查看依赖树
mvn dependency:analyze         # 分析依赖
```

---

## 快速诊断脚本

创建 `diagnose.sh` 脚本：

```bash
#!/bin/bash

echo "=== 系统诊断 ==="
echo "Java 版本: $(java -version 2>&1 | head -1)"
echo "Maven 版本: $(mvn -v | head -1)"
echo "端口 2580 占用: $(lsof -i :2580 2>/dev/null | wc -l) 个进程"

echo -e "\n=== 环境变量 ==="
echo "DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY:+已设置}"

echo -e "\n=== 数据库检查 ==="
pg_isready -h localhost -p 5432 2>/dev/null && echo "PostgreSQL: 运行中" || echo "PostgreSQL: 未运行"
```

---

*最后更新: 2026-02-24*