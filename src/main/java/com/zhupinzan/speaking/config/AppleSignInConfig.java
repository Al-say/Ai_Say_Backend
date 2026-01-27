package com.zhupinzan.speaking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Apple登录配置类
 * <p>
 * 该配置类用于管理Apple Sign In服务相关的配置信息。Apple Sign In是苹果提供的
 * 统一登录认证服务，允许用户使用Apple ID快速登录第三方应用。该配置类主要用于
 * 配置JWT令牌的签名验证和本地令牌管理。
 * </p>
 *
 * <h3>设计意图</h3>
 * <ul>
 *   <li>集中管理Apple登录服务的配置参数</li>
 *   <li>提供JWT令牌的签名密钥配置</li>
 *   <li>支持自定义令牌生命周期管理</li>
 *   <li>通过Spring配置属性机制实现配置的灵活注入</li>
 * </ul>
 *
 * <h3>主要配置项</h3>
 * <ul>
 *   <li><b>clientId</b>: Apple登录客户端ID，在Apple Developer Console中注册</li>
 *   <li><b>issuer</b>: 令牌颁发者URL，默认为Apple官方地址</li>
 *   <li><b>jwksUrl</b>: JSON Web Key Set URL，用于获取公钥验证签名</li>
 *   <li><b>tokenSecret</b>: 本地JWT令牌的签名密钥</li>
 *   <li><b>tokenTtlSeconds</b>: 本地令牌的生存时间（秒），默认7天</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * # application.yml
 * apple:
 *   signin:
 *     clientId: com.your.bundle.id
 *     tokenSecret: your-secret-key-here
 *     tokenTtlSeconds: 604800  # 7 days
 * }</pre>
 *
 * <h3>技术实现要点</h3>
 * <ul>
 *   <li>使用@ConfigurationProperties实现配置属性自动绑定</li>
 *   <li>通过prefix="apple.signin"指定配置前缀</li>
 *   <li>使用@Data注解自动生成getter/setter</li>
 *   <li>提供默认值，简化配置</li>
 * </ul>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li><b>密钥安全</b>: tokenSecret必须妥善保管，建议通过安全配置管理</li>
 *   <li><b>令牌过期</b>: 定期检查令牌过期时间，及时刷新</li>
 *   <li><b>签名验证</li>: 使用Apple提供的公钥验证JWT签名</li>
 *   <li><b>最小权限</li>: 配置必要的最小权限范围</li>
 * </ul>
 *
 * <h3>与外部服务的集成</h3>
 * <p>
 * Apple Sign In集成主要包括以下两个层面：
 * </p>
 * <ul>
 *   <li><b>客户端集成</b>: 前端使用Apple JavaScript SDK或原生SDK处理登录流程</li>
 *   <li><b>服务器端集成</li>: 验证Apple返回的JWT令牌，创建本地会话</li>
 * </ul>
 * <p>
 * 服务器端集成流程：
 * </p>
 * <ol>
 *   <li>客户端获取Apple授权码</li>
 *   <li>使用授权码交换Apple JWT令牌</li>
 *   <li>验证JWT令牌的签名和有效期</li>
 *   <li>提取用户信息（sub, email, name等）</li>
 *   <li>生成本地会话令牌</li>
 *   <li>返回本地令牌给客户端</li>
 * </ol>
 *
 * <h3>配置最佳实践</h3>
 * <ul>
 *   <li><b>客户端ID配置</b>: 确保与Apple Developer Console中注册的Bundle ID一致</li>
 *   <li><b>密钥轮换</b>: 定期更新tokenSecret，避免长期使用同一密钥</li>
 *   <li><b>令牌过期</li>: 根据业务需求调整tokenTtlSeconds</li>
 *   <li><b>域名验证</li>: 确保Apple Developer Console中配置了回调域名</li>
 *   <li><b>错误处理</li>: 实现完善的错误处理机制</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @Autowired
 * private AppleSignInConfig appleSignInConfig;
 *
 * // 验证Apple JWT令牌
 * public boolean verifyAppleToken(String token) {
 *     // 使用Apple提供的公钥验证令牌
 *     // 这里需要实现具体的验证逻辑
 * }
 *
 * // 生成本地令牌
 * public String generateLocalToken(String appleSub, String email) {
 *     // 使用tokenSecret签名生成本地令牌
 *     // 这里需要实现具体的生成逻辑
 * }
 * }</pre>
 *
 * <h3>相关依赖</h3>
 * <p>
 * 本配置类通常与以下类配合使用：
 * </p>
 * <ul>
 *   <li><b>AuthTokenFilter</b>: 使用tokenSecret验证本地JWT令牌</li>
 *   <li><b>AppleSignInController</b>: 处理Apple登录回调</li>
 *   <li><b>JWT工具类</b>: 处令牌的生成和验证</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "apple.signin")
@Component
@Data
public class AppleSignInConfig {
    /**
     * Apple登录客户端ID
     * <p>
     * 在Apple Developer Console中注册的客户端标识符，必须与应用的Bundle ID完全匹配。
     * 这个ID用于标识你的应用在Apple系统中的唯一性。
     * </p>
     */
    private String clientId;

    /**
     * 令牌颁发者URL
     * <p>
     * 指定JWT令牌的颁发者地址。默认值为Apple官方地址，
     * 用于验证JWT中的iss声明。
     * </p>
     */
    private String issuer = "https://appleid.apple.com";

    /**
     * JSON Web Key Set URL
     * <p>
     * 获取Apple公钥的URL地址。用于验证JWT令牌的签名，
     * 通过下载Apple的公钥集来验证令牌的真实性。
     * </p>
     */
    private String jwksUrl = "https://appleid.apple.com/auth/keys";

    /**
     * 本地JWT令牌的签名密钥
     * <p>
     * 用于签名本地生成的JWT令钥，确保令牌的完整性和真实性。
     * 这个密钥只在本地使用，不与Apple共享。
     * </p>
     * <p>
     * 安全注意事项：
     * <ul>
     *   <li>必须妥善保管，不要硬编码在代码中</li>
     *   <li>建议使用强密钥，至少32字符长度</li>
     *   <li>定期轮换密钥，避免长期使用同一密钥</li>
     * </ul>
     * </p>
     */
    private String tokenSecret;

    /**
     * 本地令牌的生存时间（秒）
     * <p>
     * 本地生成的JWT令牌的有效期，默认为7天（604800秒）。
     * 过期后需要重新生成令牌。
     * </p>
     * <p>
     * 业务考虑：
     * <ul>
     *   <li>平衡安全性和用户体验：太短频繁登录，太久安全风险高</li>
     *   <li>可以根据业务需求调整，如敏感操作设置较短时间</li>
     *   <li>建议设置合理的刷新机制</li>
     * </ul>
     * </p>
     */
    private long tokenTtlSeconds = 604800; // 7 days
}
