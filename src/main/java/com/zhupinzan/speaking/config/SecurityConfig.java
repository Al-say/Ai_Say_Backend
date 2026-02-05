package com.zhupinzan.speaking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置
 * <p>
 * 配置认证和授权规则，允许公开访问注册和登录端点，
 * 其他API端点需要JWT认证。
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     *
     * @param http HTTP安全配置
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF，因为我们使用JWT
            .csrf(csrf -> csrf.disable())

            // 配置会话管理为无状态
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 公开访问的端点
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/apple").permitAll()
                .requestMatchers("/api/auth/me").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()

                // 其他API端点需要认证
                .requestMatchers("/api/**").authenticated()

                // 静态资源公开访问
                .anyRequest().permitAll()
            );

        return http.build();
    }
}