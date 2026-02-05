package com.zhupinzan.speaking.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j; // Added Slf4j import

/**
 * 身份认证上下文工具类，提供从HTTP请求中获取当前认证用户信息的便捷方法。
 *
 * <h3>设计理念</h3>
 * <p>该工具类采用静态方法设计模式，封装了HTTP请求中用户信息的提取逻辑。
 * 通过将底层的request属性访问细节抽象化，为上层业务逻辑提供了简洁、统一的用户信息访问接口。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>提供统一的用户身份信息访问入口</li>
 *   <li>隐藏HTTP请求属性操作的复杂性</li>
 *   <li>确保用户信息获取的安全性（空值检查）</li>
 *   <li>支持在不同层级（Service、Controller、Filter）中获取用户信息</li>
 * </ul>
 *
 * <h3>工作机制</h3>
 * <p>1. AuthTokenFilter在认证成功后将用户信息存入request属性</p>
 * <p>2. 业务代码通过AuthContext静态方法安全地提取这些信息</p>
 * <p>3. 方法内部自动进行null检查和类型转换</p>
 * <p>4. 返回格式化的用户身份信息</p>
 *
 * <h3>支持的用户信息</h3>
 * <ul>
 *   <li>userId: 系统用户ID，存储在request.getAttribute("auth.userId")</li>
 *   <li>appleSub: Apple用户标识，存储在request.getAttribute("auth.appleSub")</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>Service层需要用户ID进行数据操作</li>
 *   <li>Controller层中需要获取Apple用户标识</li>
 *   <li>Filter链中传递用户上下文信息</li>
 *   <li>日志记录中标记操作用户</li>
 *   <li>在非Controller层（如定时任务）中获取用户信息</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>优先使用工具类而非直接访问request属性</li>
 *   <li>使用前检查返回值是否为null</li>
 *   <li>在Service层使用，避免Controller层的重复逻辑</li>
 *   <li>配合try-catch处理可能的类型转换异常</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与AuthTokenFilter：用户信息的存储方</li>
 *   <li>与CurrentUser注解：提供用户信息的数据源</li>
 *   <li>与CurrentUserInfo：作为信息提取的目标类型</li>
 *   <li>与Spring Security：集成认证授权框架</li>
 *   <li>与业务Service：为服务层提供用户上下文</li>
 * </ul>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>使用final类和私有构造函数防止实例化</li>
 *   <li>静态方法设计，无需对象状态</li>
 *   <li>内部进行null安全检查，避免NPE</li>
 *   <li>使用String类型保持灵活性</li>
 *   <li>约定属性名前缀"auth."避免命名冲突</li>
 * </ul>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // 在Service中获取当前用户ID
 * @Service
 * public class OrderService {
 *     public void createOrder(HttpServletRequest request) {
 *         String userId = AuthContext.getUserId(request);
 *         if (userId != null) {
 *             // 使用userId创建订单
 *             Order order = new Order(userId, ...);
 *             orderRepository.save(order);
 *         }
 *     }
 * }
 *
 * // 在Controller中获取Apple标识
 * @RestController
 * @RequestMapping("/api/profile")
 * public class ProfileController {
 *     @GetMapping("/apple")
 *     public ResponseEntity<String> getAppleId(HttpServletRequest request) {
 *         String appleSub = AuthContext.getAppleSub(request);
 *         return ResponseEntity.ok(appleSub != null ? appleSub : "Not found");
 *     }
 * }
 * }
 * </pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>确保AuthTokenFilter已正确设置request属性</li>
 *   <li>方法返回可能为null，调用方需要处理</li>
 *   <li>属性名约定必须保持一致</li>
 *   <li>适用于已认证的请求，未认证请求返回null</li>
 * </ul>
 *
 * @see com.zhupinzan.speaking.config.AuthTokenFilter
 * @see CurrentUserInfo
 * @see CurrentUser
 */
@Slf4j // Added Slf4j for logging
public final class AuthContext {

    // 私有构造函数，防止该工具类被实例化。
    private AuthContext() {}

    /**
     * 从HTTP请求中获取当前用户的ID。
     *
     * @param request HTTP请求对象。
     * @return 用户的ID字符串，如果请求中不存在该信息，则返回null。
     */
    public static String getUserId(HttpServletRequest request) {
        var v = request.getAttribute("auth.userId");
        return v == null ? null : v.toString();
    }

    /**
     * 从HTTP请求中获取当前用户的ID（Long类型）。
     *
     * @param request HTTP请求对象。
     * @return 用户的ID（Long类型），如果请求中不存在该信息或无法转换为Long，则返回null。
     */
    public static Long getUserIdAsLong(HttpServletRequest request) {
        var v = request.getAttribute("auth.userId");
        if (v == null) {
            return null;
        }
        if (v instanceof Long) {
            return (Long) v;
        }
        if (v instanceof String) {
            try {
                return Long.parseLong((String) v);
            } catch (NumberFormatException e) {
                log.warn("请求属性 'auth.userId' 无法转换为Long类型: {}", v, e);
                return null;
            }
        }
        log.warn("请求属性 'auth.userId' 类型不匹配: {}", v);
        return null;
    }

    /**
     * 从HTTP请求中获取当前用户的Apple 'sub' (唯一标识符)。
     *
     * @param request HTTP请求对象。
     * @return 用户的Apple 'sub'字符串，如果请求中不存在该信息，则返回null。
     */
    public static String getAppleSub(HttpServletRequest request) {
        var v = request.getAttribute("auth.appleSub");
        return v == null ? null : v.toString();
    }
}