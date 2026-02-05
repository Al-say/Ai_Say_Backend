package com.zhupinzan.speaking.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 用户账户实体类
 *
 * 【实体业务含义】
 * UserAccount 是AI口语练习应用的统一用户身份管理实体，采用基于Apple ID的认证体系。
 * 该实体负责维护用户的基本信息、身份验证状态和登录行为数据，是整个系统的用户管理中心。
 *
 * 【核心作用】
 * - 用户身份统一管理
 * - 第三方认证集成（Apple ID）
 * - 用户基本信息维护
 * - 登录行为追踪
 * - 设备关联管理
 *
 * 【设计特点】
 * - 无密码设计，完全依赖第三方认证
 * - 支持多设备使用，通过device_id管理
 * - 自动维护时间戳，确保数据时效性
 * - 轻量级设计，专注于身份管理
 */
@Entity
@Table(name = "user_account")
@Data
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键ID - 自增长
     * 【业务含义】用户账户的唯一标识符
     * 【数据类型】Long - 支持大规模用户数据
     * 【业务规则】自动生成，作为所有用户相关数据的关联基础
     */
    private Long id;

    @Column(name = "apple_sub", unique = true, length = 128)
    /**
     * Apple用户标识符
     * 【业务含义】Apple ID的用户唯一标识符（Subject）
     * 【数据类型】String(128) - JWT中的sub字段值
     * 【业务规则】可为空，支持传统登录方式
     * 【认证机制】使用Apple Sign-In的JWT进行身份验证
     * 【设计考虑】支持多种登录方式
     */
    private String appleSub;

    @Column(name = "username", unique = true, length = 64)
    /**
     * 用户名
     * 【业务含义】传统登录方式的用户名
     * 【数据类型】String(64) - 用户名格式
     * 【业务规则】可为空，支持Apple登录方式
     * 【认证机制】用户名+密码登录
     * 【设计考虑】支持多种登录方式
     */
    private String username;

    @Column(name = "password_hash", length = 256)
    /**
     * 密码哈希
     * 【业务含义】传统登录方式的密码哈希
     * 【数据类型】String(256) - BCrypt哈希格式
     * 【业务规则】可为空，支持Apple登录方式
     * 【安全机制】使用BCrypt加密存储
     * 【设计考虑】支持多种登录方式
     */
    private String passwordHash;

    @Column(name = "email", length = 256)
    /**
     * 用户邮箱地址
     * 【业务含义】从Apple ID获取的用户邮箱
     * 【数据类型】String(256) - 标准邮箱格式
     * 【业务规则】可为空，Apple ID可能不提供邮箱
     * 【用途】用于通知、找回等功能（如支持）
     * 【数据来源】Apple Sign-In的JWT claims
     */
    private String email;

    @Column(name = "email_verified")
    /**
     * 邮箱验证状态
     * 【业务含义】标识用户邮箱是否已经验证
     * 【数据类型】Boolean - true/false
     * 【业务规则】可为空，表示未验证或不适用
     * 【用途】控制需要邮箱验证的功能访问权限
     * 【实现】可能通过Apple Sign-In的email_verified claim同步
     */
    private Boolean emailVerified;

    @Column(name = "display_name", length = 128)
    /**
     * 用户显示名称
     * 【业务含义】在应用中显示的用户友好名称
     * 【数据类型】String(128) - 支持中文、英文等
     * 【业务规则】可为空，使用默认值或Apple提供的名称
     * 【用途】个性化用户体验，用户界面显示
     * 【数据来源】可来自Apple Sign-In的name claim或用户自定义
     */
    private String displayName;

    @Column(name = "avatar_url", length = 512)
    /**
     * 用户头像URL
     * 【业务含义】用户头像的可访问地址
     * 【数据类型】String(512) - 支持较长URL
     * 【业务规则】可为空
     */
    private String avatarUrl;

    @Column(name = "phone", length = 32)
    /**
     * 用户手机号
     * 【业务含义】用户绑定的手机号
     * 【数据类型】String(32) - 国际号码兼容
     * 【业务规则】可为空
     */
    private String phone;

    @Column(name = "bio", length = 512)
    /**
     * 用户签名/简介
     * 【业务含义】用户个性签名或简介
     * 【数据类型】String(512)
     * 【业务规则】可为空
     */
    private String bio;

    @Column(name = "device_id", length = 64)
    /**
     * 当前设备标识符
     * 【业务含义】用户当前使用的设备ID
     * 【数据类型】String(64) - 设备指纹或UUID
     * 【业务规则】可为空，表示设备信息未记录
     * 【用途】支持多设备使用，数据同步和设备管理
     * 【关联】与Device实体关联，但不强制外键约束（灵活设计）
     */
    private String deviceId;

    @Column(name = "created_at", nullable = false)
    /**
     * 账户创建时间
     * 【业务含义】用户首次使用应用的时间戳
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】必填，由系统自动填充
     * 【用途】用户生命周期分析，注册统计
     */
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    /**
     * 账户信息最后更新时间
     * 【业务含义】用户基本信息最后修改的时间
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】必填，自动更新
     * 【用途】数据同步，缓存失效判断
     */
    private OffsetDateTime updatedAt;

    @Column(name = "last_login_at", nullable = false)
    /**
     * 最后登录时间
     * 【业务含义】用户最后一次登录应用的时间
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】必填，自动更新
     * 【用途】用户活跃度分析，会话管理
     * 【监控】用于识别异常登录行为
     */
    private OffsetDateTime lastLoginAt;

    /**
     * 实体创建前触发
     * 【功能】初始化所有时间戳字段
     * 【逻辑】将创建时间、更新时间、最后登录时间统一设置为当前时间
     * 【业务价值】确保新账户的基础数据完整性和一致性
     */
    @PrePersist
    protected void onCreate() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (lastLoginAt == null) lastLoginAt = now;
    }

    /**
     * 实体更新前触发
     * 【功能】更新时间戳字段
     * 【逻辑】仅更新updatedAt为当前时间
     * 【设计原则】保持lastLoginAt的独立性，由登录逻辑单独控制
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
