# 1. 基础镜像：使用轻量级的 Java 21
FROM openjdk:21-jdk-slim

# 2. 设定时区为中国上海 (避免日志时间差 8 小时)
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 设置工作目录
WORKDIR /app

# 4. 将 Maven 构建好的 jar 放入容器
# 假设 target 下只有一个 jar 包
COPY target/*.jar app.jar

# 5. 暴露端口 (与你 application.properties 里的 server.port 一致)
EXPOSE 2580

# 6. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]