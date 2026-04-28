#!/bin/bash

echo "🔍 前端登录问题诊断脚本"
echo "=========================="

# 1. 检查后端服务状态
echo -e "\n📡 检查后端服务状态..."
curl -s http://localhost:2580/api/v1/evaluate/health >/dev/null
if [ $? -eq 0 ]; then
    echo "✅ 后端服务运行正常"
else
    echo "❌ 后端服务未运行或无法访问"
    echo "   请先运行: mvn spring-boot:run"
    exit 1
fi

# 2. 测试登录接口
echo -e "\n🔐 测试登录接口..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:2580/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser2","password":"test123"}')

if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
    echo "✅ 登录接口正常"
    # 提取token用于后续测试
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
else
    echo "❌ 登录接口异常"
    echo "响应: $LOGIN_RESPONSE"
    exit 1
fi

# 3. 测试受保护接口
echo -e "\n🔒 测试受保护接口..."
PROTECTED_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:2580/api/auth/me)

if echo "$PROTECTED_RESPONSE" | grep -q "userId"; then
    echo "✅ JWT认证正常"
else
    echo "❌ JWT认证失败"
    echo "响应: $PROTECTED_RESPONSE"
fi

# 4. 检查CORS
echo -e "\n🌐 检查CORS配置..."
CORS_RESPONSE=$(curl -s -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -X OPTIONS http://localhost:2580/api/auth/login -w "%{http_code}")

if [ "$CORS_RESPONSE" = "200" ]; then
    echo "✅ CORS配置正常"
else
    echo "⚠️  CORS可能有问题，响应码: $CORS_RESPONSE"
fi

# 5. 网络连通性测试
echo -e "\n📶 网络连通性测试..."
echo "模拟器访问测试 (localhost):"
curl -s http://localhost:2580/api/v1/evaluate/health | head -1

echo -e "\n真机访问测试 (本机IP):"
# 获取本机IP
LOCAL_IP=$(ip route get 1 | awk '{print $7; exit}')
if [ -n "$LOCAL_IP" ]; then
    echo "本机IP: $LOCAL_IP"
    curl -s http://$LOCAL_IP:2580/api/v1/evaluate/health 2>/dev/null | head -1 || echo "无法从外部IP访问"
else
    echo "无法获取本机IP"
fi

echo -e "\n📋 前端配置检查清单:"
echo "1. AppConfig.baseURL = \"http://localhost:2580\" (模拟器)"
echo "2. 或 AppConfig.baseURL = \"http://$LOCAL_IP:2580\" (真机)"
echo "3. 确保请求头包含: Content-Type: application/json"
echo "4. 登录成功后存储 accessToken 到 UserDefaults"
echo "5. 后续请求添加: Authorization: Bearer <token>"

echo -e "\n🛠️  如果仍有问题，请检查:"
echo "- Xcode控制台的NetworkLogger输出"
echo "- 后端日志中的请求记录"
echo "- UserDefaults中的token存储"