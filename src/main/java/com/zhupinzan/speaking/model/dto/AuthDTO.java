package com.zhupinzan.speaking.model.dto;

/**
 * 认证相关的数据传输对象 (DTO) 集合
 *
 * <p>本类包含所有与用户认证相关的数据传输对象，用于处理用户登录、设备绑定和认证响应。
 * 采用 Java Record 结构确保数据不可变性和简洁性。</p>
 *
 * <p><b>设计理念：</b></p>
 * <ul>
 *   <li>数据不可变 - 使用 Java Record 确保所有对象不可变</li>
 *   <li>类型安全 - 明确的字段类型和约束</li>
 *   <li>分层设计 - 请求对象、响应对象、数据对象分离</li>
 *   <li>前端友好 - 字段名称与 JSON 契约一致</li>
 * </ul>
 *
 * <p><b>数据流程：</b></p>
 * <ul>
 *   <li>Apple 登录请求 → 验证并生成认证响应</li>
 *   <li>设备绑定请求 → 绑定设备并返回用户信息</li>
 *   <li>认证响应 → 包含令牌和用户详细信息</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>所有字段使用默认 Jackson 序列化策略</li>
 *   <li>日期时间使用 ISO-8601 格式</li>
 *   <li>枚举使用字符串值进行序列化</li>
 * </ul>
 *
 * <p><b>验证规则：</b></p>
 * <ul>
 *   <li>idToken: 必填，有效的 Apple ID Token</li>
 *   <li>deviceId: 必填，UUID 格式</li>
 *   <li>displayName: 可选，长度限制</li>
 *   <li>email: 必须符合邮箱格式</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public class AuthDTO {

    /**
     * Apple 登录请求对象
     *
     * <p><b>作用：</b>封装用户通过 Apple 登录时提交的数据</p>
     * <p><b>数据契约：</b>用于接收前端发送的 Apple 登录请求</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>用户点击 "使用 Apple 登录" 按钮时</li>
     *   <li>Apple 授权回调时发送到后端</li>
     *   <li>设备首次登录时提供设备信息</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>idToken</b> (String, 必填)
     *     <ul>
     *       <li>作用：Apple 身份验证令牌</li>
     *       <li>格式：JWT 格式的字符串</li>
     *       <li>约束：非空、长度不超过 2048 字符</li>
     *       <li>验证：需验证 JWT 签名和过期时间</li>
     *     </ul>
     *   </li>
     *   <li><b>deviceId</b> (String, 必填)
     *     <ul>
     *       <li>作用：唯一标识用户设备</li>
     *       <li>格式：UUID 或设备唯一标识符</li>
     *       <li>约束：非空、长度不超过 255 字符</li>
     *       <li>用途：用于设备管理和会话跟踪</li>
     *     </ul>
     *   </li>
     *   <li><b>displayName</b> (String, 可选)
     *     <ul>
     *       <li>作用：用户显示名称</li>
     *       <li>来源：Apple 账户的用户名或昵称</li>
     *       <li>约束：长度不超过 100 字符</li>
     *       <li>处理：为空时使用默认值或生成唯一标识</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>错误处理：</b></p>
     * <ul>
     *   <li>idToken 无效：返回 401 Unauthorized</li>
     *   <li>deviceId 格式错误：返回 400 Bad Request</li>
     *   <li>网络异常：返回 503 Service Unavailable</li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * {
     *   "idToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
     *   "deviceId": "550e8400-e29b-41d4-a716-446655440000",
     *   "displayName": "张三"
     * }
     * </pre>
     */
    public record AppleLoginReq(
        String idToken,
        String deviceId,
        String displayName
    ) {}

    /**
     * 设备绑定请求对象
     *
     * <p><b>作用：</b>绑定用户设备到现有账户的数据传输对象</p>
     * <p><b>数据契约：</b>用于前端发送设备绑定请求</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>用户在同一账号下的多设备间切换</li>
     *   <li>设备丢失后重新绑定新设备</li>
     *   <li>跨平台使用同一账号</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>deviceId</b> (String, 必填)
     *     <ul>
     *       <li>作用：要绑定的设备唯一标识</li>
     *       <li>格式：UUID 或设备唯一标识符</li>
     *       <li>约束：非空、长度不超过 255 字符</li>
     *       <li>验证：确保设备未重复绑定</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>业务逻辑：</b></p>
     * <ul>
     *   <li>验证设备是否已被其他用户绑定</li>
     *   <li>记录设备绑定时间和类型</li>
     *   <li>更新用户活跃设备列表</li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * {
     *   "deviceId": "550e8400-e29b-41d4-a716-446655440001"
     * }
     * </pre>
     */
    public record BindDeviceReq(
        String deviceId
    ) {}

    /**
     * 认证用户信息对象
     *
     * <p><b>作用：</b>封装认证成功后的用户详细信息</p>
     * <p><b>数据契约：</b>用于在不同服务间传递用户身份信息</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>认证服务返回的用户基本信息</li>
     *   <li>设备绑定后的用户状态更新</li>
     *   <li>JWT Token 中的用户数据载体</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>userId</b> (Long, 必填)
     *     <ul>
     *       <li>作用：系统内用户唯一标识</li>
     *       <li>格式：自增主键 ID</li>
     *       <li>约束：正整数、非空</li>
     *       <li>用途：数据库主键、业务操作关联</li>
     *     </ul>
     *   </li>
     *   <li><b>appleSub</b> (String, 必填)
     *     <ul>
     *       <li>作用：Apple 用户唯一标识</li>
     *       <li>格式：Apple 提供的用户唯一 ID</li>
     *       <li>约束：非空、长度不超过 255 字符</li>
     *       <li>用途：Apple 登录身份标识</li>
     *     </ul>
     *   </li>
     *   <li><b>email</b> (String, 可选)
     *     <ul>
     *       <li>作用：用户电子邮箱</li>
     *       <li>格式：标准邮箱格式</li>
     *       <li>约束：可选、长度不超过 255 字符</li>
     *       <li>来源：Apple 账户邮箱（用户可能未公开）</li>
     *     </ul>
     *   </li>
     *   <li><b>emailVerified</b> (Boolean, 必填)
     *     <ul>
     *       <li>作用：邮箱是否已验证标志</li>
     *       <li>值：true（已验证）/ false（未验证）</li>
     *       <li>用途：验证权限和通知发送资格</li>
     *       <li>注意：即使邮箱为空，此字段也可能为 false</li>
     *     </ul>
     *   </li>
     *   <li><b>displayName</b> (String, 可选)
     *     <ul>
     *       <li>作用：用户显示名称</li>
     *       <li>格式：字符串（支持中文、英文、数字）</li>
     *       <li>约束：长度不超过 100 字符</li>
     *       <li>用途：界面显示、个性化内容</li>
     *     </ul>
     *   </li>
     *   <li><b>deviceId</b> (String, 必填)
     *     <ul>
     *       <li>作用：当前活跃设备标识</li>
     *       <li>格式：UUID 或设备唯一标识</li>
     *       <li>约束：非空、长度不超过 255 字符</li>
     *       <li>用途：设备管理、会话跟踪</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>数据关系：</b></p>
     * <ul>
     *   <li>userId 是数据库主键，其他字段用于业务逻辑</li>
     *   <li>appleSub 可能相同，但 userId 必须唯一</li>
     *   <li>deviceId 关联设备管理模块</li>
     * </ul>
     */
    public record AuthUser(
        Long userId,
        String appleSub,
        String email,
        Boolean emailVerified,
        String displayName,
        String deviceId
    ) {}

    /**
     * 传统登录请求对象
     */
    public record LoginReq(
        String username,
        String password
    ) {}

    /**
     * 用户注册请求对象
     */
    public record RegisterReq(
        String username,
        String password,
        String email,
        String displayName
    ) {}

    /**
     * 认证响应对象
     *
     * <p><b>作用：</b>封装认证成功后返回给客户端的数据</p>
     * <p><b>数据契约：</b>前端登录成功后接收的完整响应</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>Apple 登录成功后的完整响应</li>
     *   <li>设备绑定成功后的状态更新</li>
     *   <li>刷新令牌后的响应更新</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>accessToken</b> (String, 必填)
     *     <ul>
     *       <li>作用：访问令牌（JWT）</li>
     *       <li>格式：Bearer Token 格式</li>
     *       <li>约束：非空、长度不超过 2048 字符</li>
     *       <li>用途：API 调用时的身份验证</li>
     *       <li>包含：用户 ID、权限、过期时间等信息</li>
     *     </ul>
     *   </li>
     *   <li><b>expiresIn</b> (long, 必填)
     *     <ul>
     *       <li>作用：令牌过期时间（秒）</li>
     *       <li>格式：Unix 时间戳（秒）</li>
     *       <li>单位：秒</li>
     *       <li>典型值：3600（1小时）、86400（24小时）</li>
     *       <li>用途：前端提示用户重新登录时间</li>
     *     </ul>
     *   </li>
     *   <li><b>user</b> (AuthUser, 必填)
     *     <ul>
     *       <li>作用：用户详细信息对象</li>
     *       <li>类型：AuthUser 类型</li>
     *       <li>约束：非空、必须包含必要字段</li>
     *       <li>包含：用户 ID、Apple ID、邮箱等信息</li>
     *       <li>用途：初始化前端用户状态</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>安全考虑：</b></p>
     * <ul>
     *   <li>accessToken 应通过 HTTPS 传输</li>
     *   <li>令牌应存储在安全位置（如 HttpOnly Cookie）</li>
     *   <li>敏感信息（如邮箱）应考虑脱敏显示</li>
     *   <li>设置合理的过期时间，防止令牌被滥用</li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "expiresIn": 3600,
     *   "user": {
     *     "userId": 12345,
     *     "appleSub": "com.apple.identity.user.12345",
     *     "email": "user@example.com",
     *     "emailVerified": true,
     *     "displayName": "张三",
     *     "deviceId": "550e8400-e29b-41d4-a716-446655440000"
     *   }
     * }
     * </pre>
     *
     * <p><b>最佳实践：</b></p>
     * <ul>
     *   <li>前端应将 accessToken 保存在安全位置</li>
     *   <li>在令牌过期前主动刷新</li>
     *   <li>使用 refreshToken 进行无缝续期（如果支持）</li>
     *   <li>用户登出时应清除本地存储的令牌</li>
     * </ul>
     */
    public record AuthResp(
        String accessToken,
        long expiresIn,
        AuthUser user
    ) {}
}
