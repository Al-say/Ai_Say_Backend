#!/bin/bash
# fix-iptables-docker.sh
# 修复 Linux 服务器上 Docker iptables DROP 规则导致的网络连接问题
# 用法: sudo bash fix-iptables-docker.sh

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; }

if [[ $EUID -ne 0 ]]; then
  error "请以 root 身份运行: sudo bash $0"
  exit 1
fi

APP_PORT=2580

echo "================================================"
echo "  Docker iptables 网络修复脚本"
echo "================================================"
echo ""

# ── Step 1: 诊断 ──────────────────────────────────────
info "Step 1: 诊断当前 iptables 状态"

echo ""
echo ">>> FORWARD chain:"
iptables -L FORWARD -n --line-numbers 2>/dev/null || true

echo ""
echo ">>> DOCKER-USER chain:"
iptables -L DOCKER-USER -n --line-numbers 2>/dev/null || true

echo ""
echo ">>> DOCKER chain:"
iptables -L DOCKER -n --line-numbers 2>/dev/null || true

echo ""

# ── Step 2: 检测 UFW ──────────────────────────────────
info "Step 2: 检查 UFW 状态"
if command -v ufw &>/dev/null; then
  UFW_STATUS=$(ufw status | head -1)
  warn "UFW 状态: $UFW_STATUS"
  if echo "$UFW_STATUS" | grep -q "active"; then
    warn "UFW 已启用，这是 Docker 网络问题的常见原因"
    warn "将为端口 $APP_PORT 添加 UFW 放行规则..."
    ufw allow $APP_PORT/tcp
    info "已添加: ufw allow $APP_PORT/tcp"

    # 修复 UFW 的 FORWARD DROP 策略（Docker 需要 FORWARD ACCEPT）
    AFTER_RULES="/etc/ufw/after.rules"
    if ! grep -q "DOCKER-USER" "$AFTER_RULES" 2>/dev/null; then
      warn "修复 UFW FORWARD 策略以兼容 Docker..."
      cat >> "$AFTER_RULES" << 'EOF'

# BEGIN UFW AND DOCKER
*filter
:ufw-user-forward - [0:0]
:DOCKER-USER - [0:0]
-A DOCKER-USER -j ufw-user-forward
-A DOCKER-USER -j RETURN -s 10.0.0.0/8
-A DOCKER-USER -j RETURN -s 172.16.0.0/12
-A DOCKER-USER -j RETURN -s 192.168.0.0/16
COMMIT
# END UFW AND DOCKER
EOF
      info "已更新 $AFTER_RULES"
    fi
  fi
else
  info "UFW 未安装，跳过"
fi

# ── Step 3: 删除 DOCKER-USER DROP 规则 ───────────────
info "Step 3: 清除 DOCKER-USER 链中的全局 DROP 规则"
# 循环删除所有 DROP 规则（可能有多条）
while iptables -L DOCKER-USER -n 2>/dev/null | grep -q "^DROP"; do
  iptables -D DOCKER-USER -j DROP 2>/dev/null && warn "已删除 DOCKER-USER 中一条 DROP 规则" || break
done

# ── Step 4: 确保 FORWARD 链允许 Docker 流量 ──────────
info "Step 4: 确保 FORWARD 链接受 docker0 网卡流量"
DOCKER_IF="docker0"
if ! iptables -C FORWARD -i "$DOCKER_IF" -j ACCEPT 2>/dev/null; then
  iptables -I FORWARD 1 -i "$DOCKER_IF" -j ACCEPT
  info "已添加: FORWARD -i $DOCKER_IF -j ACCEPT"
fi
if ! iptables -C FORWARD -o "$DOCKER_IF" -j ACCEPT 2>/dev/null; then
  iptables -I FORWARD 2 -o "$DOCKER_IF" -j ACCEPT
  info "已添加: FORWARD -o $DOCKER_IF -j ACCEPT"
fi

# ── Step 5: 添加端口放行 INPUT 规则 ──────────────────
info "Step 5: 确保 INPUT 链放行端口 $APP_PORT"
if ! iptables -C INPUT -p tcp --dport "$APP_PORT" -j ACCEPT 2>/dev/null; then
  iptables -I INPUT 1 -p tcp --dport "$APP_PORT" -j ACCEPT
  info "已添加: INPUT -p tcp --dport $APP_PORT -j ACCEPT"
else
  info "端口 $APP_PORT INPUT 规则已存在"
fi

# ── Step 6: 重启 Docker 使其重建 iptables 规则 ───────
info "Step 6: 重启 Docker 服务以重建网络规则"
systemctl restart docker
sleep 3
info "Docker 重启完成"

# ── Step 7: 保存 iptables 规则（可选，防重启失效）────
info "Step 7: 持久化 iptables 规则"
if command -v iptables-save &>/dev/null; then
  if command -v netfilter-persistent &>/dev/null; then
    netfilter-persistent save
    info "已通过 netfilter-persistent 保存规则"
  elif command -v iptables-persistent &>/dev/null; then
    iptables-save > /etc/iptables/rules.v4
    info "已保存到 /etc/iptables/rules.v4"
  else
    warn "未找到 iptables-persistent，规则可能在重启后失效"
    warn "安装方法: apt install iptables-persistent 或 yum install iptables-services"
    iptables-save > /tmp/iptables-backup-$(date +%Y%m%d%H%M%S).rules
    info "临时备份已保存到 /tmp/"
  fi
fi

# ── Step 8: 验证 ─────────────────────────────────────
echo ""
info "Step 8: 验证端口 $APP_PORT 连通性"
sleep 2
if curl -sf "http://127.0.0.1:$APP_PORT/actuator/health" -o /dev/null; then
  echo -e "${GREEN}✅ 本地访问 http://127.0.0.1:$APP_PORT/actuator/health 成功${NC}"
else
  warn "本地访问失败，应用可能还未启动，稍候再试"
fi

LOCAL_IP=$(hostname -I | awk '{print $1}')
if curl -sf --max-time 5 "http://$LOCAL_IP:$APP_PORT/actuator/health" -o /dev/null; then
  echo -e "${GREEN}✅ 内网 IP 访问 http://$LOCAL_IP:$APP_PORT/actuator/health 成功${NC}"
else
  warn "内网 IP 访问超时，请继续排查防火墙或安全组配置"
fi

echo ""
echo "================================================"
info "修复完成！如仍有问题，请检查："
echo "  1. 云服务器安全组是否开放 $APP_PORT 端口"
echo "  2. 运行: docker-compose up -d 重新启动容器"
echo "  3. 查看 iptables 最终状态:"
echo "     iptables -L FORWARD -n && iptables -L DOCKER-USER -n"
echo "================================================"
