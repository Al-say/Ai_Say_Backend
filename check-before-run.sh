#!/bin/bash

echo "🔍 AI 口语评估系统 - 运行前诊断"
echo "================================="

# 检查 Java 版本
echo "📋 检查 Java 版本..."
java_version=$(java -version 2>&1 | head -1)
if [[ $java_version == *"21"* ]]; then
    echo "✅ Java 21 已安装: $java_version"
else
    echo "❌ 需要 Java 21，当前: $java_version"
    exit 1
fi

# 检查端口占用
echo -e "\n📋 检查端口 2580..."
if lsof -i :2580 >/dev/null 2>&1; then
    echo "⚠️  端口 2580 被占用（应用可能已在运行）"
    echo "   如需重新启动，请先停止当前应用："
    echo "   lsof -i :2580"
    echo "   kill -9 <PID>"
    echo ""
    echo "   或者使用不同端口启动："
    echo "   mvn spring-boot:run -Dspring-boot.run.arguments='--server.port=8080'"
else
    echo "✅ 端口 2580 可用"
fi

# 检查环境变量
echo -e "\n📋 检查环境变量..."
if [ -z "$DEEPSEEK_API_KEY" ]; then
    echo "⚠️  DEEPSEEK_API_KEY 未设置，将使用默认值"
else
    echo "✅ DEEPSEEK_API_KEY 已设置"
fi

# 检查 Maven
echo -e "\n📋 检查 Maven..."
if command -v mvn >/dev/null 2>&1; then
    echo "✅ Maven 已安装: $(mvn -v | head -1)"
else
    echo "❌ Maven 未安装"
    exit 1
fi

echo -e "\n🎉 所有检查通过！可以安全启动应用："
echo "   mvn spring-boot:run"
echo ""
echo "📖 如遇问题，请查看: TROUBLESHOOTING.md"