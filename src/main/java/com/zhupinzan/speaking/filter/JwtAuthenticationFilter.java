package com.zhupinzan.speaking.filter;

import com.zhupinzan.speaking.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 认证过滤器 - Spring Security 的守门人
 * <p>
 * <b>核心职责：</b>
 * • 从请求头中提取 JWT Token
 * • 验证 Token 的合法性
 * • 将用户信息注入 Spring Security 上下文
 * • 决定请求是否被放行
 * <p>
 * <b>工作流程：</b>
 * <pre>
 * 1. 客户端请求 → 携带 Authorization: Bearer {token}
 * 2. 过滤器拦截 → 提取 Token
 * 3. 调用 JwtUtil 验证 → 提取用户邮箱
 * 4. 构造 Authentication 对象 → 注入 SecurityContext
 * 5. 继续执行过滤器链 → 业务逻辑层可通过 @AuthenticationPrincipal 获取用户
 * </pre>
 * <p>
 * <b>架构位置：</b>
 * <pre>
 * 请求 → JwtAuthenticationFilter → Spring Security 过滤器链 → Controller
 * </pre>
 * <p>
 * <b>设计特点：</b>
 * • 继承 OncePerRequestFilter：确保每个请求只执行一次
 * • 无状态验证：不依赖 Session，完全基于 Token
 * • 优雅降级：Token 无效时不抛异常，交给 Spring Security 处理
 * <p>
 * <b>异常处理：</b>
 * • Token 缺失：放行请求，后续由 Spring Security 拦截（401 Unauthorized）
 * • Token 无效：记录日志，清空 SecurityContext，返回 401
 * • Token 过期：记录日志，返回 401
 * <p>
 * <b>注意事项：</b>
 * ⚠️ 此过滤器必须在 SecurityConfig 中注册，否则不生效
 * ⚠️ 公开接口（如 /api/auth/login）应在 SecurityConfig 中放行
 * ⚠️ Token 验证失败后，SecurityContext 会被清空，确保安全
 *
 * @author system
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 核心过滤逻辑
     * <p>
     * <b>执行步骤：</b>
     * 1. 从 Authorization 头提取 Token
     * 2. 验证 Token 并提取用户邮箱
     * 3. 构造 Spring Security 的 Authentication 对象
     * 4. 注入 SecurityContext，后续可通过 SecurityContextHolder 获取
     * 5. 继续执行过滤器链
     * </p>
     *
     * @param request     HTTP 请求对象
     * @param response    HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头中提取 Token
        String token = extractTokenFromRequest(request);

        // 2. 如果 Token 不存在，直接放行（后续由 Spring Security 处理）
        if (token == null) {
            log.debug("请求未携带 Token: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. 验证 Token 并提取用户邮箱
            String email = jwtUtil.validateTokenAndGetEmail(token);

            // 4. 检查 SecurityContext 是否已有认证信息（避免重复认证）
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. 构造 Spring Security 的 Authentication 对象
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,  // Principal：用户标识
                                null,   // Credentials：密码（已验证，不需要）
                                Collections.emptyList()  // Authorities：权限列表（暂不使用）
                        );

                // 6. 设置请求详情（IP、Session ID 等）
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 7. 将认证信息注入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 认证成功: email={}, uri={}", email, request.getRequestURI());
            }

        } catch (JwtException e) {
            // Token 无效或过期，清空 SecurityContext
            SecurityContextHolder.clearContext();
            log.warn("JWT 验证失败: uri={}, error={}", request.getRequestURI(), e.getMessage());
            // 不抛出异常，继续执行过滤器链，让 Spring Security 处理（返回 401）
        }

        // 8. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token
     * <p>
     * <b>标准格式：</b>
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIi...
     * </pre>
     * </p>
     * <p>
     * <b>提取规则：</b>
     * • 必须以 "Bearer " 开头（注意空格）
     * • 去除前缀后返回纯 Token 字符串
     * </p>
     *
     * @param request HTTP 请求对象
     * @return JWT Token 字符串，如果不存在或格式错误则返回 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // 去除 "Bearer " 前缀
        }

        return null;
    }
}
