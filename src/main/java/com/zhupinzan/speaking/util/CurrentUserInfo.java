package com.zhupinzan.speaking.util;

/**
 * 当前登录用户信息的数据载体（DTO）。
 *
 * <h3>设计理念</h3>
 * <p>该类采用Java 16的Record特性，实现了一个简洁、不可变的数据传输对象。
 * 通过final字段和构造函数保证数据完整性，确保在多线程环境下的安全性。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>作为Controller层与业务层之间的数据桥梁</li>
 *   <li>封装当前用户的最小必要信息，避免不必要的字段暴露</li>
 *   <li>提供类型安全的用户身份信息访问</li>
 *   <li>支持多种认证方式（内部ID + Apple标识）</li>
 * </ul>
 *
 * <h3>字段设计</h3>
 * <ul>
 *   <li>userId: 系统内部生成的唯一用户标识，用于数据库关联</li>
 *   <li>appleSub: Apple身份验证提供的唯一标识符，支持第三方登录</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>Controller方法参数注入，获取当前用户身份</li>
 *   <li>业务逻辑中标识操作主体</li>
 *   <li>日志记录中的用户上下文</li>
 *   <li>数据审计和追踪</li>
 *   <li>权限控制的基础依据</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>直接使用record类型，无需额外getter/setter</li>
 *   <li>避免在对象中添加业务逻辑，保持其纯粹的数据载体特性</li>
 *   <li>当需要更多信息时，创建新的DTO而不是扩展此对象</li>
 *   <li>使用时注意null检查，确保认证流程完整性</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与CurrentUser注解：定义注入接口</li>
 *   <li>与CurrentUserArgumentResolver：实现对象创建和注入</li>
 *   <li>与AuthTokenFilter：从JWT令牌中提取用户信息</li>
 *   <li>与AuthContext：提供静态方法获取用户信息</li>
 *   <li>与用户实体类：作为轻量级的身份标识，不包含敏感信息</li>
 * </ul>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>使用String类型而非Long，避免JSON序列化问题</li>
 *   <li>只包含必要字段，遵循最小权限原则</li>
 *   <li>使用Java Record特性，简化代码并保证不可变性</li>
 *   <li>设计为无状态，适合在多个线程间共享</li>
 * </ul>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // Controller中使用
 * @GetMapping("/orders")
 * public ResponseEntity<List<Order>> getOrders(@CurrentUser CurrentUserInfo currentUser) {
 *     // 使用currentUser.getId()查询用户订单
 *     return ResponseEntity.ok(orderService.findByUserId(currentUser.getUserId()));
 * }
 *
 * // 服务层中使用
 * @Service
 * public class OrderService {
 *     public List<Order> findByUserId(String userId) {
 *         // 使用userId查询数据库
 *         return orderRepository.findByUserId(userId);
 *     }
 * }
 * }
 * </pre>
 *
 * @param userId   系统内部的用户唯一ID，用于数据库关联和业务操作。
 * @param appleSub 用户通过"Sign in with Apple"登录时，Apple提供的唯一用户标识符，用于第三方身份验证。
 *
 * @see CurrentUser
 * @see com.zhupinzan.speaking.config.CurrentUserArgumentResolver
 * @see AuthContext
 */
public record CurrentUserInfo(
    String userId,
    String appleSub
) {}
