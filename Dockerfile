# 1. 基础镜像：改用 eclipse-temurin:21-jre (专门用于运行 Java 程序的轻量级镜像)
FROM eclipse-temurin:21-jre

# 2. 设定时区为中国上海
ENV TZ=Asia/Shanghai
# 由于基础镜像变了，设置时区的命令可能需要微调，但在 Debian/Ubuntu 基础版(默认)上通常自带时区文件
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 3. 设置工作目录
WORKDIR /app

# 4. 复制 Jar 包
COPY target/*.jar app.jar

# 5. 暴露端口
EXPOSE 2580

# 6. 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]