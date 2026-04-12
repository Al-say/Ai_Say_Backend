package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应 DTO
 * <p>
 * <b>用途：</b>登录成功后返回给客户端的数据。
 * <p>
 * <b>核心字段：</b>
 * • token：JWT Token，客户端需保存并在后续请求中携带
 * • expiresIn：Token 有效期（秒），用于客户端倒计时
 * • tokenType：固定为 "Bearer"
 * <p>
 * <b>客户端使用示例：</b>
 * <pre>
 * // 保存 Token
 * localStorage.setItem("token", response.token);
 *
 * // 后续请求携带
 * Authorization: Bearer {token}
 * </pre>
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

    /**
     * JWT Token 字符串
     * <p>客户端需保存此 Token，并在后续请求的 Authorization 头中携带</p>
     */
    private String token;

    /**
     * Token 类型（固定为 "Bearer"）
     * <p>符合 OAuth 2.0 标准</p>
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token 有效期（秒）
     * <p>客户端可用于倒计时或自动刷新</p>
     */
    private Long expiresIn;

    /**
     * 用户邮箱（可选）
     * <p>方便客户端显示当前登录用户</p>
     */
    private String email;

    /**
     * 用户昵称（可选）
     */
    private String nickname;

    /**
     * 创建简化版响应（仅包含 Token）
     */
    public static TokenResponse of(String token) {
        return TokenResponse.builder()
                .token(token)
                .build();
    }

    /**
     * 创建完整响应
     */
    public static TokenResponse of(String token, Long expiresIn, String email, String nickname) {
        return TokenResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .email(email)
                .nickname(nickname)
                .build();
    }
}
