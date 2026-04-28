#!/bin/bash

echo "🐳 AI 口语评估系统 - Docker 启动脚本"
echo "====================================="

# 检查 Docker 是否安装
echo "📋 检查 Docker..."
if ! command -v docker >/dev/null 2>&1; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否可用
echo "📋 检查 Docker Compose..."
if ! command -v docker-compose >/dev/null 2>&1; then
    echo "❌ Docker Compose 未安装"
    exit 1
fi

# 检查 .env 文件是否存在
echo "📋 检查环境配置文件..."
if [ ! -f ".env" ]; then
    echo "⚠️  .env 文件不存在，正在从模板创建..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo "✅ 已创建 .env 文件，请编辑其中的 API 密钥"
        echo "   vi .env  # 或使用其他编辑器"
        echo ""
        echo "⚠️  重要：请确保以下密钥已正确配置："
        echo "   - DEEPSEEK_API_KEY"
        echo "   - BAIDU_APP_ID, BAIDU_API_KEY, BAIDU_SECRET_KEY"
        echo ""
        read -p "是否已配置好 .env 文件？(y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "请先配置 .env 文件，然后重新运行此脚本"
            exit 1
        fi
    else
        echo "❌ .env.example 模板文件不存在"
        exit 1
    fi
else
    echo "✅ .env 文件存在"
fi

# 检查端口占用
echo "📋 检查端口占用..."
ports=(2580 5433 9000 9001)
for port in "${ports[@]}"; do
    if lsof -i :$port >/dev/null 2>&1; then
        echo "⚠️  端口 $port 被占用"
        case $port in
            2580) echo "   应用端口被占用，可能有其他应用在运行" ;;
            5433) echo "   PostgreSQL端口被占用" ;;
            9000) echo "   MinIO端口被占用" ;;
            9001) echo "   MinIO控制台端口被占用" ;;
        esac
    else
        echo "✅ 端口 $port 可用"
    fi
done

# 停止可能存在的旧容器
echo -e "\n🛑 清理旧容器..."
docker-compose down

# 构建并启动服务
echo -e "\n🚀 构建并启动服务..."
docker-compose up --build -d

# 等待服务启动
echo -e "\n⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo -e "\n📊 检查服务状态..."
docker-compose ps

# 验证应用健康状态
echo -e "\n🏥 验证应用健康状态..."
max_attempts=10
attempt=1

while [ $attempt -le $max_attempts ]; do
    echo "尝试 $attempt/$max_attempts..."
    if curl -s http://localhost:2580/actuator/health | grep -q '"status":"UP"'; then
        echo "✅ 应用启动成功！"
        break
    fi

    if [ $attempt -eq $max_attempts ]; then
        echo "❌ 应用启动失败，请检查日志："
        echo "   docker-compose logs a_say_backend"
        exit 1
    fi

    sleep 5
    ((attempt++))
done

echo -e "\n🎉 Docker 环境启动完成！"
echo ""
echo "🌐 服务访问地址："
echo "   后端API:    http://localhost:2580"
echo "   MinIO控制台: http://localhost:9001 (admin/admin)"
echo "   PostgreSQL:  localhost:5433"
echo ""
echo "📋 常用命令："
echo "   查看日志:     docker-compose logs -f"
echo "   停止服务:     docker-compose down"
echo "   重启服务:     docker-compose restart"
echo ""
echo "📖 如遇问题，请查看: TROUBLESHOOTING.md"