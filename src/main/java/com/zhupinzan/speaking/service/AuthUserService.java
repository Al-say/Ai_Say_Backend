package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.repository.UserAccountRepository;
import com.zhupinzan.speaking.repository.DeviceRepository;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import com.zhupinzan.speaking.model.entity.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户认证服务 - 设备绑定与身份验证管理
 * <p>
 * 该服务负责处理用户的设备绑定和身份验证逻辑，是系统中安全性的重要组成部分。
 * 它通过严格的参数校验和数据一致性检查，确保每个用户只能绑定一个设备，
 * 并且每个设备只能关联一个用户账号。
 * <p>
 * <b>核心职责：</b><br>
 * 1. 用户身份验证：验证登录用户的有效性和权限<br>
 * 2. 设备绑定管理：实现用户与设备的一对一绑定关系<br>
 * 3. 数据完整性：保证外键约束和数据一致性<br>
 * 4. 安全校验：防止非法的用户ID和设备ID
 * <p>
 * <b>设计特点：</b><br>
 * • 严格的参数校验：对输入参数进行多重验证<br>
 * • 异常规范化：统一抛出ResponseStatusException<br>
 * • 事务安全：依赖Repository的事务管理<br>
 * • 状态一致性：确保数据库状态与业务逻辑一致
 */
@Service
@Slf4j
public class AuthUserService {

    /**
     * 用户账号数据仓库
     * <p>
     * 负责用户账号的CRUD操作，包括：
     * • 用户信息查询和验证
     * • 设备ID的更新和管理
     * • 数据完整性约束检查
     */
    private final UserAccountRepository userAccountRepository;

    /**
     * 设备信息数据仓库
     * <p>
     * 负责设备信息的持久化管理，提供：
     * • 设备记录的upsert操作
     * • 设备活跃状态更新
     * • 设备与用户的关联关系维护
     */
    private final DeviceRepository deviceRepository;

    /**
     * 构造函数注入 - 依赖注入模式
     * <p>
     * 通过构造函数注入所有依赖的Repository，确保：
     * • 依赖不可变：final字段保证不变性
     * • 依赖清晰：明确显示服务所需的所有依赖
     * • 测试友好：便于Mock测试
     */
    public AuthUserService(UserAccountRepository userAccountRepository, DeviceRepository deviceRepository) {
        this.userAccountRepository = userAccountRepository;
        this.deviceRepository = deviceRepository;
    }

  // ====== 核心认证方法 ======

    /**
     * 用户账号验证方法 - 获取有效的用户账号实体
     * <p>
     * 此方法实现了完整用户身份验证流程，包括：
     * • 登录状态检查：验证用户是否已登录
     * • 用户ID格式校验：确保ID为有效的Long类型
     * • 账号存在性验证：确认用户账号确实存在
     * <p>
     * <b>安全措施：</b><br>
     * • 多层校验：null检查、空字符串检查、格式验证<br>
     * • 异常规范化：统一使用ResponseStatusException<br>
     * • 错误信息友好：向用户提供清晰的错误提示<br>
     * • 状态码准确：401表示未认证，400表示请求错误
     *
     * @param user 当前登录用户的上下文信息
     * @return UserAccount 验证通过的用户账号实体
     * @throws ResponseStatusException 当用户未登录、ID无效或账号不存在时
     */
    public UserAccount requireAccount(CurrentUserInfo user) {
        // 【第一层】登录状态校验
        if (user == null || user.userId() == null || user.userId().isBlank()) {
            log.warn("用户未登录或登录信息为空");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }

        // 【第二层】用户ID格式校验
        Long userId;
        try {
            userId = Long.parseLong(user.userId());
            if (userId <= 0) {
                throw new NumberFormatException("用户ID必须为正数");
            }
        } catch (NumberFormatException e) {
            log.warn("用户ID格式错误: {}", user.userId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效的用户ID格式");
        }

        // 【第三层】账号存在性验证
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户ID不存在: {}", userId);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户账号不存在");
                });
    }

    /**
     * 设备ID获取方法 - 确保用户已绑定设备
     * <p>
     * 此方法在用户身份验证的基础上，进一步检查设备绑定状态。
     * 它确保只有已绑定设备的用户才能执行需要设备标识的操作。
     * <p>
     * <b>业务逻辑：</b><br>
     * • 先验证用户身份（复用requireAccount方法）<br>
     * • 再检查设备绑定状态<br>
     * • 返回有效的设备ID字符串<br>
     * <p>
     * <b>使用场景：</b><br>
     • 音频评估：需要设备ID关联评估记录<br>
     • 进度更新：需要设备ID更新学习统计<br>
     • 数据查询：需要设备ID过滤用户数据
     *
     * @param user 当前登录用户的上下文信息
     * @return String 已绑定的设备ID
     * @throws ResponseStatusException 当用户未绑定设备时（400 Bad Request）
     */
    public String requireDeviceId(CurrentUserInfo user) {
        // 获取用户账号实体
        var account = requireAccount(user);

        // 检查设备绑定状态
        if (account.getDeviceId() == null || account.getDeviceId().isBlank()) {
            log.warn("用户未绑定设备，用户ID: {}", account.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先绑定设备后再进行操作");
        }

        log.debug("设备验证通过，设备ID: {}", account.getDeviceId());
        return account.getDeviceId();
    }

    /**
     * 设备绑定方法 - 建立用户与设备的关联关系
     * <p>
     * 此方法实现了用户绑定设备的核心业务逻辑，确保：
     * • 设备信息持久化：自动创建或更新设备记录
     * • 数据一致性：原子性的设备绑定操作
     * • 绑定唯一性：一个设备只能绑定一个用户
     * <p>
     * <b>执行流程：</b><br>
     * 1. 设备ID有效性校验<br>
     * 2. 设备记录的upsert操作<br>
     * 3. 用户身份验证<br>
     * 4. 设备ID更新到用户账号<br>
     * 5. 数据持久化保存
     * <p>
     * <b>事务特性：</b><br>
     * • 依赖Spring Data JPA的默认事务管理<br>
     * • 操作具有原子性：要么全部成功，要么全部回滚<br>
     * • 自动脏检查：JPA会自动检测实体变化
     *
     * @param user 当前登录用户的上下文信息
     * @param deviceId 要绑定的设备唯一标识
     * @throws ResponseStatusException 当输入无效或操作失败时
     */
    public void bindDevice(CurrentUserInfo user, String deviceId) {
        // 【第一层】设备ID有效性校验
        if (deviceId == null || deviceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "设备ID不能为空");
        }

        // 清理设备ID，去除前后空格
        String cleanedDeviceId = deviceId.trim();
        log.info("开始绑定设备，设备ID: {}", cleanedDeviceId);

        // 【第二层】设备记录管理 - 使用upsert确保设备存在
        deviceRepository.upsertTouch(cleanedDeviceId);

        // 【第三层】用户身份验证
        if (user == null || user.userId() == null || user.userId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再绑定设备");
        }

        Long userId;
        try {
            userId = Long.parseLong(user.userId());
            if (userId <= 0) {
                throw new NumberFormatException("用户ID必须为正数");
            }
        } catch (NumberFormatException e) {
            log.warn("用户ID格式错误: {}", user.userId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效的用户ID格式");
        }

        // 【第四层】获取用户账号并更新设备绑定
        var account = userAccountRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在，ID: {}", userId);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户账号不存在");
                });

        // 检查设备是否已被其他用户绑定
        String existingDeviceId = account.getDeviceId();
        if (existingDeviceId != null && !existingDeviceId.equals(cleanedDeviceId)) {
            log.warn("设备已被其他用户绑定，当前设备ID: {}, 原绑定ID: {}",
                    cleanedDeviceId, existingDeviceId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "设备已被其他用户绑定");
        }

        // 执行设备绑定
        account.setDeviceId(cleanedDeviceId);
        userAccountRepository.save(account);

        log.info("设备绑定成功，用户ID: {}, 设备ID: {}", userId, cleanedDeviceId);
    }

    /**
     * 更新用户账号信息
     */
    public UserAccount updateAccount(UserAccount account) {
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账户不能为空");
        }
        return userAccountRepository.save(account);
    }
}
