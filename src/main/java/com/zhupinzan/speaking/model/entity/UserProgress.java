package com.zhupinzan.speaking.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 用户进度实体类
 *
 * 【实体业务含义】
 * UserProgress 是AI口语练习应用的用户学习进度管理实体，通过device_id关联具体用户设备。
 * 该实体负责追踪用户的学习行为数据，为个性化学习推荐和成就系统提供数据支撑。
 *
 * 【核心作用】
 * - 学习行为数据追踪
 * - 用户学习进度量化
 * - 连续学习天数统计
 - 学习时长管理
 * - 个性化推荐数据基础
 *
 * 【设计特点】
 * - 按设备管理进度，支持多设备同步
 * - 轻量级设计，专注于核心进度指标
 * - 使用LocalDate进行日期管理，支持连续天数统计
 * - 时间戳管理确保数据时效性
 */
@Entity
@Table(name = "user_progress")
@Data
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键ID - 自增长
     * 【业务含义】用户进度记录的唯一标识符
     * 【数据类型】Long - 支持大规模数据存储
     * 【业务规则】自动生成，用于数据管理和关联
     */
    private Long id;

    @Column(name="device_id", nullable=false, unique=true, length=64)
    /**
     * 设备标识符
     * 【业务含义】关联到具体用户设备，每个设备独立管理进度
     * 【数据类型】String(64) - UUID或设备指纹
     * 【业务规则】必填且唯一，确保每个设备进度独立
     * 【设计考虑】支持用户多设备使用，数据可能需要同步
     * 【关联】与UserAccount和AssessmentRecord通过device_id关联
     */
    private String deviceId;

    @Column(name="total_attempts", nullable=false)
    /**
     * 总练习次数
     * 【业务含义】用户在该设备上的累计评估次数
     * 【数据类型】Integer - 正整数
     * 【业务规则】必填，最小值0
     * 【用途】衡量用户活跃度和学习投入
     * 【更新逻辑】每次新的评估记录时递增
     */
    private Integer totalAttempts;

    @Column(name="total_duration_ms", nullable=false)
    /**
     * 总练习时长（毫秒）
     * 【业务含义】用户在该设备上的累计学习时间
     * 【数据类型】Long - 毫秒数
     * 【业务规则】必填，最小值0
     * 【用途】量化学习时间，用于学习习惯分析
     * 【单位】毫秒（ms），便于精确统计和转换
     */
    private Long totalDurationMs;

    @Column(name="last_active_date")
    /**
     * 最后活跃日期
     * 【业务含义】用户最后一天进行练习的日期
     * 【数据类型】LocalDate - 不含时间的日期
     * 【业务规则】可为空，表示尚无练习记录
     * 【用途】连续学习天数统计的基础数据
     * 【设计】使用LocalDate而非OffsetDateTime，专注于日期而非具体时间
     */
    private LocalDate lastActiveDate;

    @Column(name="streak_days", nullable=false)
    /**
     * 连续学习天数
     * 【业务含义】用户连续练习的天数（成就系统核心指标）
     * 【数据类型】Integer - 正整数
     * 【业务规则】必填，最小值0（表示中断）
     * 【业务价值】激励用户持续学习，形成良好习惯
     * 【计算逻辑】基于last_active_date计算连续天数
     * 【用途】成就解锁，排行榜显示，学习激励
     */
    private Integer streakDays;

    @Column(name="updated_at", nullable=false)
    /**
     * 进度更新时间
     * 【业务含义】进度数据最后修改的时间戳
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】必填，自动更新
     * 【用途】数据同步缓存失效，进度数据时效性判断
     */
    private OffsetDateTime updatedAt;
}