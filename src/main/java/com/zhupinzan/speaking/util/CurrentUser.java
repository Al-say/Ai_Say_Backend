package com.zhupinzan.speaking.util;

import java.lang.annotation.*;

/**
 * 自定义参数注解，用于在Controller方法中注入当前登录的用户信息。
 *
 * <h3>设计理念</h3>
 * <p>此注解采用Spring MVC的HandlerMethodArgumentResolver机制，实现了用户身份信息的透明注入。
 * 通过将认证逻辑与业务逻辑分离，遵循了关注点分离(Separation of Concerns)的设计原则。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>提供统一的用户身份信息注入接口</li>
 *   <li>隐藏底层认证细节，简化Controller层代码</li>
 *   <li>确保所有需要用户信息的接口都能正确获取身份</li>
 * </ul>
 *
 * <h3>工作机制</h3>
 * <p>1. 在Controller方法参数上使用@CurrentUser注解</p>
 * <p>2. Spring MVC检测到该注解，自动调用CurrentUserArgumentResolver</p>
 * <p>3. ArgumentResolver从请求上下文或认证过滤器中获取用户信息</p>
 * <p>4. 将用户信息封装成CurrentUserInfo对象注入到方法参数中</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要获取当前登录用户ID的业务接口</li>
 *   <li>需要进行权限验证的Controller方法</li>
 *   <li>日志记录中需要用户信息的场景</li>
 *   <li>数据审计时的用户关联</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>只在需要用户身份信息的Controller方法参数上使用</li>
 *   <li>避免在不需要认证的接口上误用</li>
 *   <li>配合权限验证注解（如@PreAuthorize）使用</li>
 *   <li>确保CurrentUserArgumentResolver正确配置</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与AuthTokenFilter：获取JWT令牌中的用户信息</li>
 *   <li>与CurrentUserArgumentResolver：实现参数注入逻辑</li>
 *   <li>与CurrentUserInfo：作为用户信息的载体对象</li>
 *   <li>与Spring Security：集成认证授权体系</li>
 * </ul>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // 获取当前用户个人信息
 * @GetMapping("/profile")
 * public ResponseEntity<User> getProfile(@CurrentUser CurrentUserInfo user) {
 *     // 'user' 参数会自动被注入当前登录用户的信息
 *     return ResponseEntity.ok(userService.findById(user.getId()));
 * }
 *
 * // 更新用户设置
 * @PutMapping("/settings")
 * public ResponseEntity<Void> updateSettings(
 *         @CurrentUser CurrentUserInfo user,
 *         @RequestBody UserSettingsRequest request) {
 *     settingsService.update(user.getId(), request);
 *     return ResponseEntity.ok().build();
 * }
 * }
 * </pre>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>使用 ElementType.PARAMETER 限制只能用于方法参数</li>
 *   <li>使用 RetentionPolicy.RUNTIME 确保运行时可被反射读取</li>
 *   <li>使用 @Documented 确保在API文档中可见</li>
 *   <li>不包含属性，保持注解简洁性</li>
 * </ul>
 *
 * @see com.zhupinzan.speaking.config.CurrentUserArgumentResolver
 * @see CurrentUserInfo
 * @see com.zhupinzan.speaking.config.AuthTokenFilter
 */
@Target(ElementType.PARAMETER)      // 该注解只能用于方法参数上
@Retention(RetentionPolicy.RUNTIME) // 该注解在运行时保留，以便通过反射读取
@Documented                         // 将此注解包含在Javadoc中
public @interface CurrentUser {
}
