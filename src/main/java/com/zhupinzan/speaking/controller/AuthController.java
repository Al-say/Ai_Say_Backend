package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.dto.AuthDTO;
import com.zhupinzan.speaking.service.AuthUserService;
import com.zhupinzan.speaking.service.AppleSignInService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 用户认证REST控制器
 * <p>
 * 该控制器处理所有与用户认证相关的API请求，包括：
 * - Apple ID登录认证
 * - 用户身份验证
 * - 设备绑定管理
 *
 * <h3>认证流程：</h3>
 * <ol>
 *     <li>用户使用Apple ID登录</li>
 *     <li>服务器验证Apple ID令牌</li>
 *     <li>创建或更新用户记录</li>
 *     <li>生成访问令牌</li>
 *     <li>返回用户信息和认证令牌</li>
 * </ol>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *     <li>使用Apple ID的JWT令牌进行身份验证</li>
 *     <li>设备ID绑定确保用户身份可追踪</li>
 *     <li>访问令牌有过期时间限制</li>
 *     <li>敏感操作需要重新认证</li>
 * </ul>
 *
 * <h3>API端点：</h3>
 * <ul>
 *     <li>POST /api/auth/apple - Apple ID登录</li>
 *     <li>GET /api/auth/me - 获取当前用户信息</li>
 *     <li>POST /api/auth/bind-device - 绑定设备ID</li>
 * </ul>
 *
 * <h3>错误处理：</h3>
 * <ul>
 *     <li>400 Bad Request - 请求参数无效</li>
 *     <li>401 Unauthorized - 认证失败或令牌无效</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * Apple ID登录服务
     * <p>
     * 负责处理与Apple ID的集成认证流程：
     * - 验证Apple ID的JWT令牌
     * - 获取用户基本信息
     * - 创建或更新本地用户账户
     * - 生成系统访问令牌
     */
    private final AppleSignInService appleSignInService;

    /**
     * 认证用户服务
     * <p>
     * 提供用户认证相关的核心功能：
     * - 用户身份管理
     * - 设备ID绑定和验证
     * - 用户进度追踪
     * - 权限控制
     */
    private final AuthUserService authUserService;

    /**
     * 构造函数（使用依赖注入）
     * <p>
     * 通过构造函数注入所需的依赖服务，遵循Spring的依赖注入最佳实践。
     * 这使得控制器可以独立于具体的实现进行单元测试。
     *
     * @param appleSignInService  Apple ID登录认证服务
     * @param authUserService    认证用户管理服务
     */
    public AuthController(AppleSignInService appleSignInService, AuthUserService authUserService) {
        this.appleSignInService = appleSignInService;
        this.authUserService = authUserService;
    }

    /**
     * Apple ID登录认证端点
     * <p>
     * 该接口处理用户通过Apple ID登录的认证流程。Apple ID登录是应用的主要认证方式，
     * 它利用Apple的身份验证系统来确保用户身份的真实性。
     *
     * <h3>认证流程：</h3>
     * <ol>
     *     <li>客户端收集用户的Apple ID认证信息</li>
     *     <li>向服务器发送Apple ID令牌</li>
     *     <li>服务器验证令牌的有效性和签名</li>
     *     <li>解析用户信息（用户名、邮箱等）</li>
     *     <li>创建或更新本地用户记录</li>
     *     <li>生成系统访问令牌</li>
     *     <li>返回用户信息和认证令牌</li>
     * </ol>
     *
     * <h3>请求结构：</h3>
     * <pre>
     * POST /api/auth/apple
     * Content-Type: application/json
     *
     * {
     *     "idToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", // Apple ID JWT令牌
     *     "deviceId": "device-123456789",                     // 设备唯一标识符
     *     "displayName": "张三"                               // 用户显示名称
     * }
     * </pre>
     *
     * <h3>响应结构：</h3>
     * <pre>
     * {
     *     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // 访问令牌
     *     "expiresIn": 3600,                                       // 令牌过期时间（秒）
     *     "user": {
     *         "userId": 12345,
     *         "appleSub": "com.apple.Account...@apple.com",
     *         "email": "user@example.com",
     *         "emailVerified": true,
     *         "displayName": "张三",
     *         "deviceId": "device-123456789"
     *     }
     * }
     * </pre>
     *
     * <h3>安全要求：</h3>
     * <ul>
     *     <li>idToken必须有效且未过期</li>
     *     <li>令牌必须由Apple的官方密钥验证</li>
     *     <li>客户端必须使用HTTPS发送请求</li>
     *     <li>设备ID应该与应用内获取的设备ID一致</li>
     * </ul>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *     <li>400 Bad Request - 令牌无效或参数缺失</li>
     *     <li>401 Unauthorized - 令牌验证失败或已过期</li>
     *     <li>500 Internal Server Error - 服务器内部错误</li>
     * </ul>
     *
     * @param req Apple ID登录请求，包含：
     *            - idToken: Apple提供的JWT认证令牌
     *            - deviceId: 设备唯一标识符
     *            - displayName: 用户显示名称（可选）
     * @return ResponseEntity 包含认证成功后的用户信息和访问令牌
     * @throws ResponseStatusException 当认证失败时抛出相应的HTTP状态异常
     */
    @PostMapping("/apple")
    public ResponseEntity<AuthDTO.AuthResp> loginWithApple(@RequestBody AuthDTO.AppleLoginReq req) {
        try {
            // 调用Apple ID登录服务处理认证流程
            return ResponseEntity.ok(appleSignInService.loginWithApple(req));
        } catch (IllegalArgumentException e) {
            // 参数错误：令牌格式无效或必需字段缺失
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            // 认证失败：令牌验证失败或其他认证相关错误
            logger.error("Apple 登录失败", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Apple 登录失败");
        }
    }

    /**
     * 获取当前用户信息端点
     * <p>
     * 该接口用于获取当前登录用户的基本信息。它通常用于：
     * - 验证用户的登录状态
     * - 获取用户在界面中需要显示的信息
     * - 客户端更新本地用户数据
     *
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>应用启动时验证用户身份</li>
     *     <li>用户信息更新后刷新本地缓存</li>
     *     <li>检查用户是否已登录</li>
     * </ul>
     *
     * <h3>响应结构：</h3>
     * <pre>
     * {
     *     "userId": 12345,
     *     "appleSub": "com.apple.Account...@apple.com",
     *     "email": "user@example.com",
     *     "emailVerified": true,
     *     "displayName": "张三",
     *     "deviceId": "device-123456789"
     * }
     * </pre>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *     <li>该接口需要有效的认证令牌</li>
     *     <li>返回的信息是实时的，可能包含最新的用户数据</li>
     *     <li>不包含敏感信息（如密码、令牌等）</li>
     * </ul>
     *
     * @param user 当前登录用户信息（通过@CurrentUser注解自动注入）
     *             该信息由认证过滤器在请求处理前自动添加
     * @return ResponseEntity 包含当前用户信息的CurrentUserInfo对象
     */
    @GetMapping("/me")
    public ResponseEntity<CurrentUserInfo> me(@CurrentUser CurrentUserInfo user) {
        // 直接返回当前用户信息，无需额外处理
        return ResponseEntity.ok(user);
    }

    /**
     * 绑定设备ID端点
     * <p>
     * 该接口用于将用户的Apple ID与特定的设备ID进行绑定。
     * 设备绑定对于以下功能非常重要：
     * - 追踪用户在不同设备上的练习数据
     * - 实现跨设备同步
     * - 防止同一用户在多个设备上的数据混淆
     *
     * <h3>绑定流程：</h3>
     * <ol>
     *     <li>验证用户已登录</li>
     *     <li>检查设备ID格式有效性</li>
     *     <li>更新用户记录中的设备ID</li>
     *     <li>同步设备相关的用户数据</li>
     * </ol>
     *
     * <h3>请求结构：</h3>
     * <pre>
     * POST /api/auth/bind-device
     * Content-Type: application/json
     *
     * {
     *     "deviceId": "device-123456789"
     * }
     * </pre>
     *
     * <h3>设备ID规范：</h3>
     * <ul>
     *     <li>格式：建议使用UUID或设备厂商提供的唯一标识</li>
     *     <li>长度：通常在36-64字符之间</li>
     *     <li>唯一性：每个设备应有唯一的ID</li>
     *     <li>持久性：设备ID在设备重置后应保持不变</li>
     * </ul>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *     <li>400 Bad Request - 设备ID格式无效或用户未登录</li>
     *     <li>401 Unauthorized - 认证失败</li>
     * </ul>
     *
     * @param user 当前登录用户信息
     * @param req 设备绑定请求，包含要绑定的设备ID
     * @return ResponseEntity 成功时返回200 OK，失败时抛出相应的HTTP异常
     * @throws ResponseStatusException 当参数无效或操作失败时抛出
     */
    @PostMapping("/bind-device")
    public ResponseEntity<?> bindDevice(@CurrentUser CurrentUserInfo user, @RequestBody AuthDTO.BindDeviceReq req) {
        try {
            // 处理设备ID绑定
            // 如果req为null，则传入null；否则传入其中的deviceId
            authUserService.bindDevice(user, req == null ? null : req.deviceId());

            // 绑定成功，返回空响应（HTTP 200 OK）
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // 参数错误：设备ID格式无效或重复
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
