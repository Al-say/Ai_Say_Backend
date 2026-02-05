package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求 DTO
 * <p>
 * <b>数据契约：</b>定义用户登录时的标准输入格式。
 * <p>
 * <b>验证规则：</b>
 * • email：必填，必须符合邮箱格式
 * • password：必填
 * <p>
 * <b>安全机制：</b>
 * • Service 层会验证密码哈希
 * • 登录成功后返回 JWT Token
 * • 失败次数过多应考虑锁定账户（未来增强）
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequest {

    /**
     * 用户邮箱（登录账号）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
