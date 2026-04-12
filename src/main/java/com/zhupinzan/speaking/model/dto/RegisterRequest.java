package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册请求 DTO
 * <p>
 * <b>数据契约：</b>定义用户注册时的标准输入格式。
 * <p>
 * <b>验证规则：</b>
 * • email：必填，必须符合邮箱格式
 * • password：必填，6-20 位
 * • nickname：可选，最多 50 位
 * <p>
 * <b>安全考虑：</b>
 * • 密码不会以明文存储，Service 层会使用 BCrypt 加密
 * • 邮箱作为唯一标识，不可重复
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest {

    /**
     * 用户邮箱（登录账号）
     * <p>必须符合标准邮箱格式，如：user@example.com</p>
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 登录密码
     * <p>
     * 长度限制：6-20 位<br>
     * 建议包含：大小写字母 + 数字 + 特殊字符
     * </p>
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在 6-20 位之间")
    private String password;

    /**
     * 用户昵称（可选）
     * <p>用于显示，可后续修改</p>
     */
    @Size(max = 50, message = "昵称长度不能超过 50 位")
    private String nickname;
}
