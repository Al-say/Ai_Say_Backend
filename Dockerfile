# 1. 基础镜像
FROM eclipse-temurin:21-jre

# 2. 设定时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# === 新增部分开始 ===
# 3. 安装 FFmpeg
# 更新软件源并安装 ffmpeg，最后清理缓存减小镜像体积
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
# === 新增部分结束 ===

# 4. 设置工作目录
WORKDIR /app

# 5. 复制 Jar 包
COPY target/*.jar app.jar

# 6. 暴露端口
EXPOSE 2580

# 7. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]