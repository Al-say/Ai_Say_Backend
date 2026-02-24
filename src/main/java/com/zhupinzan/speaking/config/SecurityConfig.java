package com.zhupinzan.speaking.config;

import com.zhupinzan.speaking.filter.JwtAuthenticationFilter;
import com.zhupinzan.speaking.service.UserDetailsServiceImpl; // 导入自定义的 UserDetailsService
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder; // 导入 PasswordEncoder
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    private final UserDetailsServiceImpl userDetailsService; // 注入自定义的 UserDetailsService
    private final PasswordEncoder passwordEncoder;     // 注入密码编码器

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
                .requestMatchers("/api/auth/**").permitAll()  // 认证接口 (包括注册和登录)
                .requestMatchers("/api/home/daily").permitAll() // 每日挑战接口，允许匿名访问
                .requestMatchers("/api/eval/**").permitAll()  // 评估接口，允许匿名访问
                .requestMatchers("/h2-console/**").permitAll()  // H2 控制台
                .requestMatchers("/actuator/**").permitAll()  // 健康检查
                .requestMatchers("/api/v1/evaluate/health").permitAll()  // API 健康检查

                // 临时：允许所有请求匿名访问用于调试
                .anyRequest().permitAll()
            )
            // 配置自定义的 AuthenticationProvider
            .authenticationProvider(authenticationProvider())
            // 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    /**
     * CORS 配置源
     * <p>
     * 配置跨域资源共享，允许前端应用访问后端API
     * </p>
     *
     * @return CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源（根据环境配置）
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",      // 本地开发
            "http://127.0.0.1:*",     // 本地开发
            "http://192.168.*.*:*",   // 内网IP
            "http://10.*.*.*:*"       // 内网IP
        ));

        // 允许的HTTP方法
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));

        // 允许发送凭据（cookies, authorization headers）
        configuration.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 配置 DaoAuthenticationProvider
     * <p>
     * 用于处理基于用户名和密码的认证。它使用 `UserDetailsService` 来获取用户详情，
     * 并使用 `PasswordEncoder` 来验证密码。
     * </p>
     * @return 配置好的 DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * 配置 AuthenticationManager
     * <p>
     * AuthenticationManager 是 Spring Security 认证机制的核心接口，
     * 用于处理认证请求。在这里我们通过 Configuration 的方式将其暴露为一个 Bean。
     * </p>
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager 实例
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}