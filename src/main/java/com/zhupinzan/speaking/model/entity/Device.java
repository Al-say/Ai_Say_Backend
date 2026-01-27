package com.zhupinzan.speaking.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备实体类
 *
 * 【实体业务含义】
 * Device 是AI口语练习应用的设备管理实体，用于追踪和管理用户使用的设备信息。
 * 该实体支持多设备场景下的数据同步和个性化设置，是用户体验管理的重要组成部分。
 *
 * 【核心作用】
 * - 设备身份管理
 * - 设备行为追踪
 * - 设备元数据存储
 * - 多设备数据同步支持
 * - 个性化设备设置管理
 *
 * 【设计特点】
 * - 设备唯一标识，防止重复注册
 * - JSONB存储设备元数据，支持灵活扩展
 * - 时间戳管理追踪设备活跃状态
 * - 轻量级设计，专注于设备管理
 */
@Entity
@Table(name = "device")
@Data
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键ID - 自增长
     * 【业务含义】设备记录的唯一标识符
     * 【数据类型】Long - 支持大规模设备数据管理
     * 【业务规则】自动生成，用于设备数据管理和关联
     */
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true, length = 64)
    /**
     * 设备唯一标识符
     * 【业务含义】设备的唯一身份标识（如UUID、设备指纹等）
     * 【数据类型】String(64) - 足够长度的唯一标识
     * 【业务规则】必填且唯一，确保设备身份唯一性
     * 【生成方式】客户端生成，服务端验证唯一性
     * 【用途】多设备管理，数据同步，个性化设置
     */
    private String deviceId;

    @Column(name = "created_at", nullable = false)
    /**
     * 设备注册时间
     * 【业务含义】设备首次被记录在系统中的时间
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】必填，由系统自动填充
     * 【用途】设备生命周期分析，统计设备注册量
     */
    private OffsetDateTime createdAt;

    @Column(name = "last_seen_at", nullable = false)
    /**
     * 最后活跃时间
     * 【业务含义】设备最后一次使用应用的时间
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】必填，自动更新
     * 【用途】设备活跃度监控，僵尸设备识别
     * 【业务价值】判断用户设备使用习惯，优化资源分配
     */
    private OffsetDateTime lastSeenAt;

    /**
     * 设备元数据
     * 【业务含义】存储设备的详细信息和使用环境数据
     * 【数据类型】JSONB - PostgreSQL原生JSON二进制格式
     * 【业务规则】必填字段，默认为空HashMap
     * 【JSONB使用场景】
     *   - 设备基本信息：设备型号、操作系统版本、屏幕分辨率
     *   - 应用信息：应用版本、Build号、安装来源
     *   - 网络环境：网络类型（WiFi/4G/5G）、地理位置（可选）
     *   - 使用偏好：语言设置、主题模式、字体大小
     *   - 设备能力：麦克风权限、摄像头权限、存储空间
     * 【设计优势】JSONB支持索引查询，便于设备分析和统计
     * 【扩展性】随时可以添加新的设备属性而不需要修改表结构
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> meta = new HashMap<>();

    /**
     * 实体创建前触发
     * 【功能】初始化设备相关时间戳
     * 【逻辑】将创建时间和最后活跃时间统一设置为当前时间
     * 【业务价值】确保新设备记录的基础数据完整
     */
    @PrePersist
    protected void onCreate() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (lastSeenAt == null) lastSeenAt = now;
    }
}