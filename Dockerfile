# 使用官方的 OpenJDK 21 镜像作为基础镜像
FROM openjdk:21-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制构建好的 JAR 文件到容器中
COPY target/*.jar app.jar

# 暴露应用端口
EXPOSE 2580

# 运行应用
CMD ["java", "-jar", "app.jar"]