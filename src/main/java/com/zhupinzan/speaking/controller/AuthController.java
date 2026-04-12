package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.dto.AuthDTO;
import com.zhupinzan.speaking.service.AuthUserService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import java.util.Map;
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
    private final AuthUserService authUserService;

    /**
     * 构造函数（使用依赖注入）
     * <p>
     * 通过构造函数注入所需的依赖服务，遵循Spring的依赖注入最佳实践。
     * 这使得控制器可以独立于具体的实现进行单元测试。
     *
     * @param authUserService    认证用户管理服务
     */
    public AuthController(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    /**
     * 用户注册端点
     * <p>
     * 该接口处理用户注册请求，支持传统用户名密码注册方式。
     * 注册成功后自动登录并返回JWT令牌。
     * </p>
     *
     * @param req 注册请求，包含用户名、密码、邮箱等信息
     * @return ResponseEntity 包含认证成功后的用户信息和访问令牌
     * @throws ResponseStatusException 当注册失败时抛出相应的HTTP异常
     */
    @PostMapping("/register")
    public ResponseEntity<AuthDTO.AuthResp> register(@RequestBody AuthDTO.RegisterReq req) {
        try {
            // 1. 验证输入参数
            if (req == null || req.username() == null || req.username().isBlank()) {
                throw new IllegalArgumentException("用户名不能为空");
            }
            if (req.password() == null || req.password().length() < 6) {
                throw new IllegalArgumentException("密码长度不能少于6位");
            }
            if (req.email() == null || req.email().isBlank()) {
                throw new IllegalArgumentException("邮箱不能为空");
            }
            // 可以添加更复杂的邮箱格式验证

            // 2. 调用注册服务，并获取认证响应
            AuthDTO.AuthResp authResp = authUserService.register(req);

            // 3. 返回认证响应
            return ResponseEntity.ok(authResp);
        } catch (IllegalArgumentException e) {
            // 参数校验失败
            logger.warn("用户注册参数无效: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            // 用户名或邮箱已存在等业务异常
            logger.warn("用户注册业务失败: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            // 其他未知异常
            logger.error("用户注册异常", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "注册失败");
        }
    }

    /**
     * 用户登录端点
     * <p>
     * 该接口处理传统用户名密码登录请求。
     * 验证用户凭据后生成JWT令牌返回。
     * </p>
     *
     * @param req 登录请求，包含用户名和密码
     * @return ResponseEntity 包含认证成功后的用户信息和访问令牌
     * @throws ResponseStatusException 当登录失败时抛出相应的HTTP异常
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDTO.AuthResp> login(@RequestBody AuthDTO.LoginReq req) {
        try {
            // 1. 验证输入参数
            if (req == null || req.username() == null || req.username().isBlank()) {
                throw new IllegalArgumentException("用户名或邮箱不能为空");
            }
            if (req.password() == null || req.password().isBlank()) {
                throw new IllegalArgumentException("密码不能为空");
            }

            // 2. 调用登录服务，并获取认证响应
            AuthDTO.AuthResp authResp = authUserService.login(req);

            // 3. 返回认证响应
            return ResponseEntity.ok(authResp);
        } catch (IllegalArgumentException e) {
            // 参数校验失败
            logger.warn("用户登录参数无效: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // 密码错误等认证失败异常
            logger.warn("用户登录凭据无效: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        } catch (RuntimeException e) {
            // 其他运行时异常，例如用户不存在
            logger.error("用户登录失败: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        } catch (Exception e) {
            // 其他未知异常
            logger.error("登录异常", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
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

            // 绑定成功，返回标准 JSON 确认
            return ResponseEntity.ok(Map.of("status", "success", "message", "Device bound successfully"));
        } catch (IllegalArgumentException e) {
            // 参数错误：设备ID格式无效或重复
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
