package com.zhupinzan.speaking.config;

import com.zhupinzan.speaking.util.AuthContext;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 自定义参数解析器，用于处理 {@link CurrentUser} 注解的参数注入。
 *
 * <h3>设计理念</h3>
 * <p>该解析器实现Spring MVC的HandlerMethodArgumentResolver接口，
 * 采用AOP（面向切面编程）的思想，在不修改Controller代码的情况下，
 * 为被@CurrentUser注解标记的参数自动注入用户信息。
 * 这种设计遵循了"约定优于配置"的原则，简化了开发流程。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>自动解析和注入当前登录用户信息</li>
 *   <li>将认证逻辑与业务逻辑解耦</li>
 *   <li>提供统一的用户信息访问接口</li>
 *   <li>确保Controller层的简洁性</li>
 *   <li>支持多种认证方式的集成</li>
 * </ul>
 *
 * <h3>工作机制</h3>
 * <p>1. Spring MVC启动时注册此参数解析器</p>
 * <p>2. 处理请求时，检查Controller方法参数</p>
 * <p>3. supportsParameter()方法判断参数是否需要此解析器处理</p>
 * <p>4. resolveArgument()方法解析参数的实际值</p>
 * <p>5. 将解析结果注入到Controller方法参数中</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>所有需要获取当前用户身份的Controller方法</li>
 *   <li>支持JWT、OAuth、第三方认证等场景</li>
 *   <li>微服务架构中的跨服务调用</li>
 *   <li>需要用户上下文传递的异步操作</li>
 *   <li>RESTful API中的权限验证接口</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>在WebConfig中正确注册此解析器</li>
 *   <li>确保Controller方法的参数类型匹配CurrentUserInfo</li>
 *   <li>配合@Valid注解进行参数校验</li>
 *   <li>在resolveArgument中处理异常情况</li>
 *   <li>保持解析逻辑的简洁性和高效性</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与CurrentUser注解：定义注入的标记</li>
 *   <li>与CurrentUserInfo：作为注入的数据载体</li>
 *   <li>与AuthContext：提供用户信息的提取逻辑</li>
 *   <li>与AuthTokenFilter：确保用户信息已存储在请求中</li>
 *   <li>与Spring MVC：集成到请求处理流程中</li>
 *   <li>与WebConfig：作为配置组件注册</li>
 * </ul>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>使用HandlerMethodArgumentResolver接口确保Spring集成</li>
 *   <li>supportsParameter方法提供灵活的条件判断</li>
 *   <li>resolveArgument方法处理各种异常情况</li>
 *   <li>复用AuthContext工具类避免代码重复</li>
 *   <li>支持非HTTP请求环境的降级处理</li>
 *   <li>使用isAssignableFrom支持类型继承</li>
 * </ul>
 *
 * <h3>方法详解：</h3>
 * <h4>supportsParameter()</h4>
 * <p>判断规则：
 * <ul>
 *   <li>参数必须有@CurrentUser注解</li>
 *   <li>参数类型必须是CurrentUserInfo或其子类</li>
 * </ul>
 * 此方法决定了哪个参数需要由这个解析器处理。</p>
 *
 * <h4>resolveArgument()</h4>
 * <p>处理流程：
 * <ul>
 *   <li>从NativeWebRequest获取HttpServletRequest</li>
 *   <li>通过AuthContext提取用户信息</li>
 *   <li>创建并返回CurrentUserInfo对象</li>
 *   <li>处理非HTTP请求的降级情况</li>
 * </ul>
 * 此方法实现了用户信息的实际提取和封装。</p>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // Controller中使用
 * @RestController
 * @RequestMapping("/api")
 * public class UserController {
 *     @GetMapping("/profile")
 *     public ResponseEntity<UserProfile> getProfile(@CurrentUser CurrentUserInfo currentUser) {
 *         // currentUser会自动被注入用户信息
 *         return ResponseEntity.ok(userService.getProfile(currentUser.getUserId()));
 *     }
 *
 *     @PostMapping("/orders")
 *     public ResponseEntity<Order> createOrder(
 *             @CurrentUser CurrentUserInfo currentUser,
 *             @Valid @RequestBody OrderRequest request) {
 *         // 同时使用用户信息和请求参数
 *         return ResponseEntity.ok(orderService.create(currentUser.getUserId(), request));
 *     }
 * }
 *
 * // WebConfig配置
 * @Configuration
 * public class WebConfig implements WebMvcConfigurer {
 *     @Override
 *     public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
 *         resolvers.add(new CurrentUserArgumentResolver());
 *     }
 * }
 * }
 * </pre>
 *
 * <h3>测试和调试</h3>
 * <p>测试要点：
 * <ul>
 *   <li>测试正常情况下的参数注入</li>
 *   <li>测试未认证请求的处理</li>
 *   <li>测试不同参数类型的兼容性</li>
 *   <li>测试异常情况的降级处理</li>
 *   <li>验证与Spring Security的集成</li>
 * </ul>
 * </p>
 *
 * <h3>性能考虑</h3>
 * <p>优化建议：
 * <ul>
 *   <li>AuthContext的方法已经做了null检查，避免重复判断</li>
 *   <li>使用轻量级的CurrentUserInfo对象</li>
 *   <li>避免在resolveArgument中执行复杂逻辑</li>
 *   <li>考虑缓存频繁访问的用户信息</li>
 * </ul>
 * </p>
 *
 * @see com.zhupinzan.speaking.util.CurrentUser
 * @see com.zhupinzan.speaking.util.CurrentUserInfo
 * @see com.zhupinzan.speaking.config.WebConfig#addArgumentResolvers(java.util.List)
 * @see com.zhupinzan.speaking.config.AuthTokenFilter
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断该解析器是否支持给定的方法参数。
     *
     * @param parameter 待检查的方法参数。
     * @return 如果参数被 {@code @CurrentUser} 注解标记，并且参数类型是 {@code CurrentUserInfo} 或其子类，则返回true。
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && CurrentUserInfo.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 解析参数的实际值。
     *
     * @param parameter     方法参数
     * @param mavContainer  ModelAndView容器
     * @param webRequest    Web请求
     * @param binderFactory 数据绑定工厂
     * @return 解析出的 {@link CurrentUserInfo} 对象。
     */
    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {
        // 从Web请求中获取底层的HttpServletRequest对象
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            // 在极少数情况下（例如，非HTTP请求环境），返回一个空的UserInfo对象
            return new CurrentUserInfo(null, null);
        }

        // 使用AuthContext工具类从请求属性中提取用户信息
        Long userId = AuthContext.getUserIdAsLong(request); // Changed to getUserIdAsLong
        String appleSub = AuthContext.getAppleSub(request);

        // 创建CurrentUserInfo实例
        return new CurrentUserInfo(userId, appleSub);
    }
}
