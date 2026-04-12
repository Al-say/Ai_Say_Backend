package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.repository.UserProgressRepository;
import com.zhupinzan.speaking.service.AuthUserService;
import com.zhupinzan.speaking.service.LoginHistoryService;
import com.zhupinzan.speaking.service.storage.LocalStorageService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.zhupinzan.speaking.model.entity.UserAccount; // Added import for UserAccount

/**
 * 用户个人中心REST控制器
 * <p>
 * 该控制器负责处理用户个人中心和统计数据相关的API请求，
 * 为用户提供个人练习数据的统计和展示功能。
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li><b>个人主页</b>：展示用户的基本信息和欢迎界面</li>
 *     <li><b>练习统计</b>：提供用户的练习数据汇总和分析</li>
 *     <li><b>进度追踪</b>：记录和展示用户的练习进度</li>
 *     <li><b>成就展示</li>：展示用户的学习成就和里程碑</li>
 * </ul>
 *
 * <h3>数据模型：</h3>
 * <ul>
 *     <li>UserProgress：用户进度实体，存储练习统计数据</li>
 *     <li>ProfileStatsDTO：统计数据传输对象，包含关键指标</li>
 * </ul>
 *
 * <h3>未来扩展：</h3>
 * <ul>
 *     <li>用户设置管理</li>
 *     <li>个人资料编辑</li>
 *     <li>学习目标设定</li>
 *     <li>成就系统接口</li>
 * </ul>
 *
 * <h3>API端点：</h3>
 * <ul>
 *     <li>GET /api/profile - 个人主页信息</li>
 *     <li>GET /api/profile/stats - 用户统计数据</li>
 * </ul>
 *
 * <h3>权限控制：</h3>
 * <ul>
 *     <li>所有端点都需要用户登录认证</li>
 *     <li>用户只能访问自己的数据</li>
 *     <li>通过设备ID进行数据隔离</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    /**
     * 用户进度数据仓库
     * <p>
     * 用于查询和管理用户的练习统计数据。该仓库提供了：
     * - findByDeviceId：按设备ID查询用户进度
     * - 自动统计用户的练习次数、时长等数据
     * - 支持连续练习天数的计算
     *
     * <h3>统计数据包含：</h3>
     * <ul>
     *     <li>练习总次数（totalAttempts）</li>
     *     <li>连续练习天数（streakDays）</li>
     *     <li>总练习时长（totalDurationMs）</li>
     *     <li>最后活跃日期（lastActiveDate）</li>
     * </ul>
     */
    private final UserProgressRepository userProgressRepo;

    /**
     * 认证用户服务
     * <p>
     * 提供用户认证和设备管理功能：
     * - 验证用户登录状态
     * - 获取设备ID
     * - 确保数据访问权限
     */
    private final AuthUserService authUserService;
    private final LocalStorageService localStorageService;
    private final LoginHistoryService loginHistoryService;

    /**
     * 构造函数（使用依赖注入）
     * <p>
     * 通过构造函数注入所需的依赖服务，遵循Spring的依赖注入最佳实践。
     * 这种设计使得控制器易于测试和维护。
     *
     * @param userProgressRepo  用户进度数据仓库
     * @param authUserService  认证用户服务
     */
    public ProfileController(UserProgressRepository userProgressRepo, AuthUserService authUserService, LocalStorageService localStorageService, LoginHistoryService loginHistoryService) {
        this.userProgressRepo = userProgressRepo;
        this.authUserService = authUserService;
        this.localStorageService = localStorageService;
        this.loginHistoryService = loginHistoryService;
    }

    /**
     * 个人中心主页API端点
     * <p>
     * 该接口是"我的"模块的入口点，用于展示个人中心的基本信息。
     * 目前作为一个占位符接口，未来可以扩展为包含用户资料、设置选项等。
     *
     * <h3>功能定位：</h3>
     * <ul>
     *     <li>作为个人中心模块的根端点</li>
     *     <li>验证用户访问权限</li>
     *     <li>提供基本的模块标识信息</li>
     *     <li>未来可扩展为完整的用户资料展示</li>
     * </ul>
     *
     * <h3>未来扩展计划：</h3>
     * <ul>
     *     <li>用户基本信息展示（头像、昵称等）</li>
     *     <li>快捷操作入口（开始练习、查看历史等）</li>
     *     <li>学习进度概览</li>
     *     <li>成就徽章展示</li>
     *     <li>设置选项链接</li>
     * </ul>
     *
     * <h3>响应示例：</h3>
     * <pre>
     * "我的模块 - 个人中心和设置"
     * </pre>
     *
     * @return ResponseEntity 包含模块标识信息
     *         - 成功：200 OK，返回字符串形式的欢迎信息
     *         - 未授权：401 Unauthorized（通过@CurrentUser自动处理）
     */
    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(@CurrentUser CurrentUserInfo user) {
        var account = authUserService.requireAccount(user);
        return ResponseEntity.ok(new ProfileDTO(
            account.getId(),
            account.getDisplayName(),
            account.getAvatarUrl(),
            account.getPhone(),
            account.getBio(),
            account.getEmail(),
            account.getEmailVerified(),
            account.getDeviceId(),
            account.getLastLoginAt() == null ? null : account.getLastLoginAt().toString()
        ));
    }

    /**
     * 个人信息传输对象 (DTO)
     */
    public record ProfileDTO(
        Long userId,
        String displayName,
        String avatarUrl,
        String phone,
        String bio,
        String email,
        Boolean emailVerified,
        String deviceId,
        String lastLoginAt
    ) {}

    /**
     * 绑定设备请求DTO
     */
    public record BindDeviceReq(String deviceId) {}

    /**
     * 更新个人资料请求DTO
     */
    public record UpdateProfileReq(String displayName, String avatarUrl, String phone, String bio) {}

    /**
     * 通用状态响应
     */
    public record StatusResp(String status) {}

    /**
     * 用户统计数据传输对象 (DTO)
     * <p>
     * 使用Java 16的Record特性定义的不可变数据载体，用于封装用户的练习统计数据。
     * 这种设计提供了简洁的类型安全性和不可变性，适合在网络间传输数据。
     *
     * <h3>统计指标说明：</h3>
     * <ul>
     *     <li><b>deviceId</b>：设备唯一标识符，用于数据追踪和隔离</li>
     *     <li><b>streakDays</b>：连续练习天数，反映用户的学习习惯和毅力</li>
     *     <li><b>lastActiveDate</b>：最后活跃日期，显示用户最近的活动时间</li>
     *     <li><b>totalAttempts</b>：总练习次数，体现用户的练习量</li>
     *     <li><b>totalDurationMs</b>：总练习时长（毫秒），反映用户的投入时间</li>
     * </ul>
     *
     * <h3>业务意义：</h3>
     * <ul>
     *     <li> streakDays是用户坚持度的核心指标</li>
     *     <li> totalAttempts/totalDurationMs反映练习频率和强度</li>
     *     <li> lastActiveDate帮助判断用户活跃度</li>
     *     <li> 所有数据用于生成用户的成就和激励信息</li>
     * </ul>
     *
     * @param deviceId         设备唯一标识符，用于数据隔离和追踪
     * @param streakDays       连续练习天数，用户坚持学习的体现
     * @param lastActiveDate   最后活跃日期，ISO-8601格式的时间字符串
     * @param totalAttempts    用户累计完成的练习次数
     * @param totalDurationMs  用户累计练习的总时长（毫秒）
     */
    public record ProfileStatsDTO(
        String deviceId,
        int streakDays,
        String lastActiveDate,
        int totalAttempts,
        long totalDurationMs
    ) {}

    /**
     * 获取用户练习统计数据API端点
     * <p>
     * 该接口用于获取用户的练习数据统计，为个人中心和成就系统提供数据支持。
     * 它整合了用户的练习次数、连续天数、总时长等关键指标，帮助用户了解自己的学习情况。
     *
     * <h3>统计维度：</h3>
     * <ul>
     *     <li><b>练习频率</b>：通过总练习次数和连续天数反映</li>
     *     <li><b>练习强度</b>：通过总练习时长体现</li>
     *     <li><b>活跃度</b>：通过最后活跃日期判断</li>
     *     <li><b>持续性</b>：通过连续练习天数展示</li>
     * </ul>
     *
     * <h3>数据计算逻辑：</h3>
     * <ol>
     *     <li>通过设备ID查询用户进度记录</li>
     *     <li>如果记录存在，使用数据库中的统计值</li>
     *     <li>如果记录不存在，返回默认值（零值）</li>
     *     <li>日期格式化为ISO-8601标准字符串</li>
     * </ol>
     *
     * <h3>响应数据结构：</h3>
     * <pre>
     * {
     *     "deviceId": "device-123456789",
     *     "streakDays": 15,
     *     "lastActiveDate": "2024-01-01T10:00:00Z",
     *     "totalAttempts": 45,
     *     "totalDurationMs": 2700000
     * }
     * </pre>
     *
     * <h3>业务应用：</h3>
     * <ul>
     *     <li>在个人中心展示用户的学习成就</li>
     *     <li>计算连续练习奖励</li>
     *     <li>生成学习报告和建议</li>
     *     <li>设置个性化学习目标</li>
     * </ul>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *     <li>无数据情况：返回零值统计，避免客户端错误</li>
     *     <li>权限问题：通过@CurrentUser自动处理</li>
     * </ul>
     *
     * @param user 当前登录用户信息（通过@CurrentUser注解注入）
     *             - 用于验证用户身份
     *             - 获取设备ID进行数据查询
     * @return ResponseEntity 包含用户统计数据
     *         - 成功：200 OK，返回ProfileStatsDTO对象
     *         - 无数据：返回零值统计，确保客户端正常显示
     *         - 未授权：401 Unauthorized（通过@CurrentUser自动处理）
     */
    @GetMapping("/stats")
    public ResponseEntity<ProfileStatsDTO> stats(@CurrentUser CurrentUserInfo user) {
        // 步骤1：验证用户身份并获取用户账号 (不再强制设备绑定)
        UserAccount account = authUserService.requireAccount(user);
        String deviceId = account.getDeviceId(); // 获取用户已绑定的设备ID

        // 步骤2：从数据库中查找用户的进度记录
        // 如果用户未绑定设备，则不查询进度，直接返回默认值
        var p = (deviceId != null) ? userProgressRepo.findByDeviceId(deviceId).orElse(null) : null;

        // 步骤3：处理查询结果
        if (p == null) {
            // 如果用户没有进度记录或未绑定设备，返回默认值
            return ResponseEntity.ok(new ProfileStatsDTO(deviceId, 0, null, 0, 0L));
        }

        // 步骤4：如果找到记录，构建并返回统计数据DTO
        return ResponseEntity.ok(new ProfileStatsDTO(
            deviceId,
            p.getStreakDays(),
            p.getLastActiveDate() == null ? null : p.getLastActiveDate().toString(),
            p.getTotalAttempts(),
            p.getTotalDurationMs()
        ));
    }

    /**
     * 获取登录历史
     */
    @GetMapping("/login-history")
    public ResponseEntity<java.util.List<com.zhupinzan.speaking.repository.LoginHistoryRepository.LoginHistoryView>> loginHistory(
            @CurrentUser CurrentUserInfo user,
            @RequestParam(defaultValue = "50") int limit
    ) {
        var account = authUserService.requireAccount(user);
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return ResponseEntity.ok(loginHistoryService.getHistory(account.getId(), safeLimit));
    }

    /**
     * 更新个人资料
     */
    @PatchMapping
    public ResponseEntity<ProfileDTO> updateProfile(@CurrentUser CurrentUserInfo user, @RequestBody UpdateProfileReq req) {
        if (req == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        var displayName = req.displayName();
        var avatarUrl = req.avatarUrl();
        var phone = req.phone();
        var bio = req.bio();
        if (displayName != null) {
            displayName = displayName.trim();
            if (displayName.isEmpty()) {
                throw new IllegalArgumentException("displayName 不能为空");
            }
            if (displayName.length() > 64) {
                throw new IllegalArgumentException("displayName 过长");
            }
        }
        if (avatarUrl != null) {
            avatarUrl = avatarUrl.trim();
            if (avatarUrl.isEmpty()) {
                throw new IllegalArgumentException("avatarUrl 不能为空");
            }
            if (avatarUrl.length() > 512) {
                throw new IllegalArgumentException("avatarUrl 过长");
            }
        }
        if (phone != null) {
            phone = phone.trim();
            if (phone.isEmpty()) {
                throw new IllegalArgumentException("phone 不能为空");
            }
            if (phone.length() > 32) {
                throw new IllegalArgumentException("phone 过长");
            }
        }
        if (bio != null) {
            bio = bio.trim();
            if (bio.isEmpty()) {
                throw new IllegalArgumentException("bio 不能为空");
            }
            if (bio.length() > 512) {
                throw new IllegalArgumentException("bio 过长");
            }
        }

        var account = authUserService.requireAccount(user);
        if (displayName != null) {
            account.setDisplayName(displayName);
        }
        if (avatarUrl != null) {
            account.setAvatarUrl(avatarUrl);
        }
        if (phone != null) {
            account.setPhone(phone);
        }
        if (bio != null) {
            account.setBio(bio);
        }
        var saved = authUserService.updateAccount(account);
        return ResponseEntity.ok(new ProfileDTO(
            saved.getId(),
            saved.getDisplayName(),
            saved.getAvatarUrl(),
            saved.getPhone(),
            saved.getBio(),
            saved.getEmail(),
            saved.getEmailVerified(),
            saved.getDeviceId(),
            saved.getLastLoginAt() == null ? null : saved.getLastLoginAt().toString()
        ));
    }

    /**
     * 绑定用户设备
     */
    @PostMapping("/device")
    public ResponseEntity<?> bindDevice(@CurrentUser CurrentUserInfo user, @RequestBody BindDeviceReq req) {
        authUserService.bindDevice(user, req == null ? null : req.deviceId());
        return ResponseEntity.ok(Map.of("status", "success", "message", "Device bound successfully"));
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<ProfileDTO> uploadAvatar(@CurrentUser CurrentUserInfo user, @RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件");
        }

        var account = authUserService.requireAccount(user);
        String ext = getFileExtension(file.getOriginalFilename(), ".png");
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("文件读取失败");
        }

        String url = localStorageService.uploadAvatar(bytes, String.valueOf(account.getId()), ext);
        account.setAvatarUrl(url);
        var saved = authUserService.updateAccount(account);
        return ResponseEntity.ok(new ProfileDTO(
            saved.getId(),
            saved.getDisplayName(),
            saved.getAvatarUrl(),
            saved.getPhone(),
            saved.getBio(),
            saved.getEmail(),
            saved.getEmailVerified(),
            saved.getDeviceId(),
            saved.getLastLoginAt() == null ? null : saved.getLastLoginAt().toString()
        ));
    }

    private String getFileExtension(String filename, String defaultExt) {
        if (filename == null || filename.isBlank()) {
            return defaultExt;
        }
        int lastIdx = filename.lastIndexOf(".");
        return lastIdx == -1 ? defaultExt : filename.substring(lastIdx);
    }
}