package com.zhupinzan.speaking.config;

import com.zhupinzan.speaking.util.JwtUtil;
import io.jsonwebtoken.Claims; // Added import for Claims
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证令牌过滤器
 * <p>
 * 该过滤器是整个应用的安全核心，负责验证所有非公开API请求的JWT令牌。
 * 它实现了基于令牌的认证机制，确保只有拥有有效令牌的用户才能访问受保护的资源。
 * 过滤器继承自OncePerRequestFilter，保证每个请求只被处理一次，避免重复认证。
 * </p>
 */
@Deprecated
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public AuthTokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // 步骤 1: 判断请求是否为公开访问路径
        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 步骤 2: 从请求头中提取Bearer Token
        var auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少 Bearer Token");
            return;
        }
        var token = auth.substring("Bearer ".length()).trim();

        try {
            // 步骤 3: 验证JWT并提取Claims
            Claims claims = jwtUtil.validateTokenAndGetClaims(token);

            // 步骤 4: 从Claims中提取用户信息
            Long userId = jwtUtil.extractUserId(token);

            if (userId == null) {
                writeUnauthorized(response, "Token 中缺少用户ID信息");
                return;
            }

            String subject = claims.getSubject(); // Get subject (email or username)

            // 步骤 5: 将用户信息存入请求属性
            request.setAttribute("auth.userId", String.valueOf(userId));
            request.setAttribute("auth.email", subject);
            request.setAttribute("auth.appleSub", null); // Apple登录已移除，此字段不再从Token获取

            // 放行请求到后续处理程序
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // JWT验证失败（过期、签名错误、格式错误等）
            writeUnauthorized(response, e.getMessage());
        } catch (Exception e) {
            // 其他未知异常
            logger.error("JWT认证过滤器异常", e);
            writeUnauthorized(response, "Token 校验异常");
        }
    }

    /**
     * 判断请求的路径是否为公开访问路径
     */
    private boolean isPublic(HttpServletRequest request) {
        var path = request.getRequestURI();

        // OPTIONS请求通常用于CORS预检，应直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 注册和登录接口
        if (path.startsWith("/api/auth/register") || path.startsWith("/api/auth/login")) {
            return true;
        }
        // 每日挑战接口（现在默认是公开的）
        if (path.startsWith("/api/home/daily")) return true;
        // Spring Boot Actuator监控端点
        if (path.startsWith("/actuator")) return true;
        // 应用根路径
        if (path.equals("/")) return true;

        return false;
    }

    /**
     * 向客户端写入401 Unauthorized错误响应
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}
