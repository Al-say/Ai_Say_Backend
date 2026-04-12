package com.zhupinzan.speaking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置
 * <p>
 * 该配置类提供密码加密和验证功能，用于传统用户名密码登录方式。
 * 使用BCrypt算法确保密码安全存储。
 * </p>
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码编码器Bean
     * <p>
     * 配置BCrypt密码编码器，用于密码加密和验证。
     * BCrypt是一种安全的单向哈希算法，包含盐值和自适应复杂度。
     * </p>
     *
     * @return BCrypt密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}