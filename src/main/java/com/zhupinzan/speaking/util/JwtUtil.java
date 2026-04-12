package com.zhupinzan.speaking.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.zhupinzan.speaking.model.entity.UserAccount; // Added import for UserAccount

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类 - 令牌的签发与验证中心
 * <p>
 * <b>核心职责：</b>
 * • 生成 JWT Token（用户登录后颁发"通行证"）
 * • 验证 Token 合法性（检查签名、过期时间）
 * • 从 Token 中提取用户信息
 * <p>
 * <b>安全机制：</b>
 * • HS256 对称加密算法
 * • Secret 从环境变量读取，杜绝硬编码
 * • Token 包含过期时间，自动失效
 * <p>
 * <b>Token 结构：</b>
 * <pre>
 * Header:    {"alg": "HS256", "typ": "JWT"}
 * Payload:   {"sub": "user@example.com", "iat": 1704441600, "exp": 1704528000}
 * Signature: HMACSHA256(base64(header) + "." + base64(payload), secret)
 * </pre>
 * <p>
 * <b>使用场景：</b>
 * • 登录成功后，调用 `generateToken(account)` 返回给客户端
 * • 客户端每次请求携带 Token（放在 Authorization 头）
 * • 服务端通过 `validateTokenAndGetClaims(token)` 验证身份并提取用户信息
 * <p>
 * <b>注意事项：</b>
 * ⚠️ JWT 是无状态的，一旦签发无法主动撤销（除非引入 Redis 黑名单）
 * ⚠️ Token 被盗即可假冒身份，必须通过 HTTPS 传输
 * ⚠️ Secret 泄露则所有 Token 失效，必须使用环境变量
 *
 * @author system
 * @since 1.0.0
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     * <p>
     * 使用 HMAC-SHA256 算法生成密钥。
     * Secret 必须足够长（至少 256 位 = 32 字符）。
     * </p>
     *
     * @return 签名密钥
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     * <p>
     * <b>Payload 包含：</b>
     * • sub（Subject）：用户邮箱或用户名，作为唯一标识
     * • userId（Custom Claim）：用户ID
     * • iat（Issued At）：签发时间
     * • exp（Expiration）：过期时间
     * </p>
     *
     * @param account 用户账户对象
     * @return JWT Token 字符串
     */
    public String generateToken(UserAccount account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String subject = account.getEmail() != null ? account.getEmail() : account.getUsername();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", account.getId());
        if (account.getAppleSub() != null && !account.getAppleSub().isBlank()) {
            claims.put("appleSub", account.getAppleSub());
        }

        String token = createToken(claims, subject, now, expiryDate);

        log.debug("生成 JWT Token: subject={}, userId={}, expiresAt={}", subject, account.getId(), expiryDate);
        return token;
    }

    /**
     * 创建 JWT Token 的辅助方法
     *
     * @param claims 自定义claims
     * @param subject 主题
     * @param issuedAt 签发时间
     * @param expiration 过期时间
     * @return JWT Token 字符串
     */
    private String createToken(Map<String, Object> claims, String subject, Date issuedAt, Date expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 Token 并返回所有 Claims
     * <p>
     * <b>验证内容：</b>
     * • 签名是否正确（防篡改）
     * • Token 是否过期
     * • 格式是否合法
     * </p>
     * <p>
     * <b>异常处理：</b>
     * • ExpiredJwtException：Token 已过期
     * • SignatureException：签名验证失败（密钥错误或被篡改）
     * • MalformedJwtException：Token 格式错误
     * • IllegalArgumentException：Token 为空或无效
     * </p>
     *
     * @param token JWT Token 字符串
     * @return Claims 对象，包含所有Payload信息
     * @throws JwtException 如果 Token 无效或过期
     */
    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token 已过期: {}", e.getMessage());
            throw new JwtException("Token 已过期，请重新登录");
        } catch (SignatureException e) {
            log.error("Token 签名验证失败: {}", e.getMessage());
            throw new JwtException("无效的 Token");
        } catch (MalformedJwtException e) {
            log.error("Token 格式错误: {}", e.getMessage());
            throw new JwtException("Token 格式错误");
        } catch (IllegalArgumentException e) {
            log.error("Token 为空或无效: {}", e.getMessage());
            throw new JwtException("Token 不能为空");
        }
    }

    /**
     * 从 Token 中提取 userId
     *
     * @param token JWT Token 字符串
     * @return userId，如果不存在或无法转换则返回null
     */
    public Long extractUserId(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) {
            return null;
        }
        // 防止 Integer/Long 类型转换问题
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            try {
                return Long.parseLong((String) userIdObj);
            } catch (NumberFormatException e) {
                log.warn("userId claim 无法转换为Long: {}", userIdObj);
                return null;
            }
        }
        log.warn("userId claim 类型不匹配: {}", userIdObj);
        return null;
    }

    /**
     * 从 Token 中提取 appleSub（用于兼容历史 owner 归属）。
     *
     * @param token JWT Token 字符串
     * @return appleSub claim，如果不存在或为空则返回 null
     */
    public String extractAppleSub(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        Object appleSubObj = claims.get("appleSub");
        if (appleSubObj == null) {
            return null;
        }
        String appleSub = String.valueOf(appleSubObj).trim();
        return appleSub.isEmpty() ? null : appleSub;
    }

    /**
     * 验证 Token 并提取用户邮箱（现在改为提取Subject）
     * <p>
     * 用于需要静默检查的场景，不会抛出异常。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return Subject (email or username) from the token
     * @throws JwtException If token is invalid or expired
     */
    public String validateTokenAndGetSubject(String token) {
        return validateTokenAndGetClaims(token).getSubject();
    }

    /**
     * 检查 Token 是否有效（不抛出异常版本）
     * <p>
     * 用于需要静默检查的场景，不会抛出异常。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return true 如果 Token 有效，否则 false
     */
    public boolean isTokenValid(String token) {
        try {
            validateTokenAndGetClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
