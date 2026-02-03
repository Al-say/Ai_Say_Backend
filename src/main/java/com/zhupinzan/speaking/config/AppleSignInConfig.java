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
 * <h3>整体作用和设计意图</h3>
 * <p>
 * 本配置类实现了应用与Apple Sign In服务的安全集成，为用户提供隐私保护的统一登录体验。
 * 通过集中管理Apple登录相关的配置参数，实现了认证逻辑与业务逻辑的解耦，提高了系统的安全性和可维护性。
 * 该设计遵循了OAuth 2.0和OpenID Connect标准，确保了认证流程的安全性和合规性。
 * </p>
 * <ul>
 *   <li><b>安全集成</b>: 集中管理Apple登录服务的配置参数，确保认证过程的安全性</li>
 *   <li><b>JWT管理</b>: 提供JWT令牌的签名密钥配置，支持本地令牌的生成和验证</li>
 *   <li><b>生命周期控制</b>: 支持自定义令牌生命周期管理，平衡安全性和用户体验</li>
 *   <li><b>灵活配置</b>: 通过Spring配置属性机制实现配置的灵活注入和环境隔离</li>
 *   <li><b>标准化实现</b>: 遵循Apple Sign In的技术规范，确保与苹果生态系统的兼容性</li>
 *   <li><b>隐私保护</b>: 支持匿名用户标识，保护用户隐私信息</li>
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
 * <h3>安全考虑和实现细节</h3>
 * <ul>
 *   <li><b>密钥安全</b>:
 *       <ul>
 *         <li>tokenSecret必须妥善保管，建议通过安全配置管理</li>
 *         <li>使用强随机算法生成密钥，避免使用弱密码</li>
 *         <li>密钥长度建议至少256位（32字节）</li>
 *         <li>实施密钥轮换机制，定期更新密钥</li>
 *         <li>使用密钥加密服务（KMS）保护密钥</li>
 *       </ul>
 *   </li>
 *   <li><b>令牌安全</b>:
 *       <ul>
 *         <li>合理设置tokenTtlSeconds，平衡安全性和用户体验</li>
 *         <li>实现令牌自动刷新机制</li>
 *         <li>记录令牌发放和撤销事件</li>
 *         <li>监控异常令牌使用模式</li>
 *       </ul>
 *   </li>
 *   <li><b>签名验证</b>:
 *       <ul>
 *         <li>使用Apple提供的公钥验证JWT签名</li>
 *         <li>缓存公钥以提高验证性能</li>
 *         <li>定期更新公钥缓存</li>
 *         <li>验证JWT的标准声明（iss, aud, exp等）</li>
 *       </ul>
 *   </li>
 *   <li><b>权限控制</b>:
 *       <ul>
 *         <li>配置必要的最小权限范围</li>
 *         <li>实现基于角色的访问控制（RBAC）</li>
 *         <li>敏感操作需要额外验证</li>
 *         <li>记录所有认证相关日志</li>
 *       </ul>
 *   </li>
 *   <li><b>隐私保护</b>:
 *       <ul>
 *         <li>尊重用户隐私选择，不收集不必要信息</li>
 *         <li>支持匿名用户标识</li>
 *         <li>合规处理用户数据</li>
 *         <li>遵循GDPR等隐私法规</li>
 *       </ul>
 *   </li>
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
 *
 * <h3>业务价值和集成收益</h3>
 * <p>
 * Apple Sign In集成为企业带来多方面的业务价值：
 * </p>
 * <ul>
 *   <li><b>用户体验提升</b>:
 *       <ul>
 *         <li>一键登录，简化注册流程</li>
 *         <li>无需记忆额外密码</li>
 *         <li>跨设备同步登录状态</li>
 *         <li>苹果生态无缝体验</li>
 *       </ul>
 *   </li>
 *   <li><b>品牌信任度</b>:
 *       <ul>
 *         <li>借助Apple品牌信誉</li>
 *         <li>提升用户对平台的信任</li>
 *         <li>展示对用户隐私的重视</li>
 *         <li>符合现代安全标准</li>
 *       </ul>
 *   </li>
 *   <li><b>运营效率提升</b>:
 *       <ul>
 *         <li>减少密码重置请求</li>
 *         <li>降低客服支持成本</li>
 *         <li>提高用户留存率</li>
 *         <li>简化账户管理流程</li>
 *       </ul>
 *   </li>
 *   <li><b>技术架构优化</b>:
 *       <ul>
 *         <li>标准化认证流程</li>
 *         <li>减少自研认证系统的维护成本</li>
 *         <li>提高系统安全性</li>
 *         <li>支持未来认证方式的扩展</li>
 *       </ul>
 *   </li>
 *   <li><b>合规与安全</b>:
 *       <ul>
 *         <li>符合苹果的技术规范要求</li>
 *         <li>遵循行业最佳安全实践</li>
 *         <li>降低安全风险</li>
 *         <li>通过安全审计</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h3>集成注意事项</h3>
 * <ul>
 *   <li><b>域名配置</b>: 确保在Apple Developer Console中正确配置回调域名</li>
 *   <li><b>Bundle ID匹配</b>: clientId必须与实际应用的Bundle ID完全一致</li>
 *   <li><b>沙盒环境测试</b>: 先在沙盒环境测试完整流程</li>
 *   <li><b>错误处理</b>: 实现完善的错误处理和用户反馈机制</li>
 *   <li><b>性能监控</b>: 监控认证API的性能和成功率</li>
 *   <li><b>用户引导</b>: 为首次使用Apple Sign In的用户提供清晰指引</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "apple.signin")
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
