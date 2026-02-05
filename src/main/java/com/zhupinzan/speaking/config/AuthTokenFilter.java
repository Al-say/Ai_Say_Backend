package com.zhupinzan.speaking.config;

import com.zhupinzan.speaking.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public AuthTokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        var auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少 Bearer Token");
            return;
        }
        var token = auth.substring("Bearer ".length()).trim();

        try {
            String email = jwtUtil.validateTokenAndGetEmail(token);
            // 简单的用户标识设置，这里的userId和appleSub可以根据业务从JWT或数据库中获取
            // 为了简化，我们假设sub就是userId
            request.setAttribute("auth.userId", email);
            request.setAttribute("auth.appleSub", null); // Apple登录已移除

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            writeUnauthorized(response, e.getMessage());
        }
    }

    private boolean isPublic(HttpServletRequest request) {
        var path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        if (path.startsWith("/api/auth/register") || path.startsWith("/api/auth/login")) {
            return true;
        }
        if (path.startsWith("/api/home/daily")) return true;
        if (path.startsWith("/actuator")) return true;
        if (path.equals("/")) return true;

        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }
}