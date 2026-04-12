#!/bin/bash
# ssl-init.sh
# 服务器 SSL 证书初始化脚本
# 用法:
#   自签名（无域名）: sudo bash ssl-init.sh self-signed
#   Let's Encrypt:   sudo bash ssl-init.sh certbot your-domain.com admin@your-domain.com

set -e
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

MODE=${1:-self-signed}
DOMAIN=${2:-localhost}
EMAIL=${3:-admin@localhost}

CERT_DIR="/etc/ssl/certs"
KEY_DIR="/etc/ssl/private"

[[ $EUID -ne 0 ]] && error "请以 root 身份运行: sudo bash $0 $*"

# ────────────────────────────────────────────────────────
# 模式 1: 自签名证书（开发/内网/无域名场景）
# ────────────────────────────────────────────────────────
if [[ "$MODE" == "self-signed" ]]; then
  info "生成自签名 SSL 证书（有效期 365 天）..."
  mkdir -p "$CERT_DIR" "$KEY_DIR"

  openssl req -x509 -newkey rsa:4096 \
    -keyout "$KEY_DIR/server.key" \
    -out    "$CERT_DIR/server.crt" \
    -days 365 -nodes \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=AiSay/CN=$DOMAIN"

  chmod 600 "$KEY_DIR/server.key"
  chmod 644 "$CERT_DIR/server.crt"

  info "证书生成完成:"
  echo "  证书: $CERT_DIR/server.crt"
  echo "  私钥: $KEY_DIR/server.key"
  echo ""
  warn "注意: 自签名证书浏览器会警告「不受信任」"
  warn "如有域名请用: sudo bash $0 certbot <域名> <邮箱>"

# ────────────────────────────────────────────────────────
# 模式 2: Let's Encrypt 免费证书（需要真实域名 + 80 端口可访问）
# ────────────────────────────────────────────────────────
elif [[ "$MODE" == "certbot" ]]; then
  [[ "$DOMAIN" == "localhost" ]] && error "Let's Encrypt 需要真实域名，用法: sudo bash $0 certbot your-domain.com admin@your-domain.com"

  info "安装 certbot..."
  if command -v apt &>/dev/null; then
    apt-get install -y certbot
  elif command -v yum &>/dev/null; then
    yum install -y certbot
  elif command -v dnf &>/dev/null; then
    dnf install -y certbot
  else
    error "无法自动安装 certbot，请手动安装"
  fi

  info "申请 Let's Encrypt 证书: $DOMAIN"
  certbot certonly --standalone \
    --non-interactive \
    --agree-tos \
    --email "$EMAIL" \
    -d "$DOMAIN"

  # 创建 nginx 期望的路径软链接
  mkdir -p "$CERT_DIR" "$KEY_DIR"
  ln -sf "/etc/letsencrypt/live/$DOMAIN/fullchain.pem" "$CERT_DIR/server.crt"
  ln -sf "/etc/letsencrypt/live/$DOMAIN/privkey.pem"   "$KEY_DIR/server.key"

  info "证书申请成功！软链接已创建"
  echo "  证书: $CERT_DIR/server.crt → /etc/letsencrypt/live/$DOMAIN/fullchain.pem"
  echo "  私钥: $KEY_DIR/server.key → /etc/letsencrypt/live/$DOMAIN/privkey.pem"

  # 设置自动续期（cron）
  info "配置自动续期（每天 3:00 检查）..."
  (crontab -l 2>/dev/null; echo "0 3 * * * certbot renew --quiet && docker-compose -f /opt/ai-backend/docker-compose.prod.yml exec nginx nginx -s reload") | crontab -
  info "自动续期已配置"

else
  error "未知模式: $MODE (可选: self-signed | certbot)"
fi

echo ""
info "SSL 证书初始化完成！现在可以启动服务:"
echo "  docker-compose -f docker-compose.prod.yml up -d"
