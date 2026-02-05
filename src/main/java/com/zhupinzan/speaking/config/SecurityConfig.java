package com.zhupinzan.speaking.config;

import com.zhupinzan.speaking.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置 - 系统安全防护墙
 * <p>
 * <b>核心职责：</b>
 * • 定义哪些接口需要认证，哪些可以公开访问
 • 配置 JWT 过滤器，实现无状态认证
 * • 提供密码加密器（BCrypt）
 * • 禁用 Session，强制使用 Token
 * <p>
 * <b>安全策略：</b>
 * • 无状态（Stateless）：不使用 Session，完全依赖 JWT
 * • CSRF 禁用：前后端分离项目，Token 机制已防止 CSRF
 * • BCrypt 加密：密码存储使用单向哈希，不可逆
 * <p>
 * <b>过滤器链顺序：</b>
 * <pre>
 * 请求 → JwtAuthenticationFilter → Spring Security 内置过滤器 → Controller
 * </pre>
 * <p>
 * <b>放行规则：</b>
 * • /api/auth/** - 认证接口（注册、登录）
 * • /h2-console/** - H2 数据库控制台（开发环境）
 * • /actuator/** - 健康检查（监控）
 * • 其他所有接口 - 需要 JWT 认证
 *
 * @author system
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤链
     * <p>
     * <b>配置项说明：</b>
     * • csrf：禁用 CSRF 保护（JWT 已提供防护）
     * • sessionManagement：强制无状态模式
     * • authorizeHttpRequests：定义接口访问规则
     * • addFilterBefore：在标准认证过滤器前插入 JWT 过滤器
     * </p>
     *
     * @param http HTTP 安全配置对象
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（前后端分离 + JWT 模式不需要）
            .csrf(AbstractHttpConfigurer::disable)

            // 配置会话管理为无状态（不创建 Session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 公开访问的端点（不需要认证）
                .requestMatchers("/api/auth/**").permitAll()  // 认证接口
                .requestMatchers("/api/home/daily").permitAll() // 每日挑战接口，允许匿名访问
                .requestMatchers("/h2-console/**").permitAll()  // H2 控制台
                .requestMatchers("/actuator/**").permitAll()  // 健康检查
                .requestMatchers("/api/v1/evaluate/health").permitAll()  // API 健康检查

                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            )

            // 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}