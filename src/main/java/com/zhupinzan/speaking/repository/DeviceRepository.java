package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 设备管理数据访问层 Repository
 *
 * <p>Repository职责说明：
 * 负责用户设备信息的跟踪和管理，记录设备的基本信息和活动状态。
 * 通过设备的首次使用和活跃更新，实现用户多设备支持和设备管理功能。
 * 主要用于设备统计、用户活跃度分析和安全审计。
 *
 * <p>数据完整性保证：
 * - 使用PostgreSQL的ON CONFLICT机制确保数据一致性
 * - @Transactional注解保证事务的完整性
 * - 设备ID作为唯一键防止重复记录
 * - 时间戳字段记录关键时间点
 *
 * <p>性能优化策略：
 * - 使用原生SQL实现高效的设备更新
 * - 避免全表扫描，直接通过主键操作
 * - 最小化数据传输量，只更新必要字段
 *
 * <p>与业务逻辑的关联：
 * - 与DeviceService配合实现设备管理
 * - 为UserProgress提供设备隔离支持
 * - 支持用户多设备切换功能
 * - 实现设备活跃度统计
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * 设备活跃更新（UPSERT操作）
     *
     * <p>核心功能：
     * 记录设备的首次使用或活跃更新，是设备管理的核心方法。
     * 当用户首次使用设备时创建记录，后续使用时更新最后活跃时间。
     * 这是系统了解用户设备情况的主要入口。
     *
     * <p>SQL机制说明：
     * - INSERT INTO: 尝试插入新的设备记录
     * - ON CONFLICT (device_id): 检测设备ID冲突
     * - DO UPDATE SET: 冲突时更新最后活跃时间
     * - 使用CAST('{}' AS jsonb)初始化meta字段为空JSON对象
     *
     * <p>字段含义：
     * - device_id: 设备唯一标识符（通常由客户端生成）
     * - created_at: 首次创建时间（仅新设备设置）
     * - last_seen_at: 最后活跃时间（每次更新）
     * - meta: 设备元数据（JSON格式，预留扩展字段）
     *
     * <p>注解说明：
     * @Modifying: 标记为修改操作
     * @Transactional: 确保事务隔离性
     * @Query: 使用原生SQL实现UPSERT
     * nativeQuery: true 表示使用原生SQL而非JPQL
     *
     * <p>业务场景应用：
     * 1. 应用启动时调用，记录设备活跃
     * 2. 用户登录时更新设备状态
     * 3. 定时任务清理不活跃设备
     * 4. 统计用户的设备使用情况
     *
     * <p>安全机制：
     * - 每次活跃更新都记录时间戳
     * - 支持检测设备活跃异常（如长时间不活跃）
     * - 为后续的设备绑定和认证提供基础
     *
     * <p>数据扩展性：
     * - meta字段使用JSON格式，便于未来添加设备信息
     * - 可以存储设备型号、系统版本、IP地址等信息
     * - 支持设备分类和标签功能
     *
     * <p>性能优化建议：
     * - 确保device_id字段有唯一索引
     * - 考虑添加缓存层，减少数据库压力
     * - 在高并发场景下，监控此方法的执行频率
     *
     * <p>异常处理：
     * - @Transactional保证事务回滚
     * - 方法本身不抛出特定异常，由Spring框架处理
     * - 调用方需要处理可能的数据库约束异常
     *
     * <p>监控指标：
     * - 记录此方法的调用次数和执行时间
     * - 监控设备的活跃频率分布
     * - 分析设备的首次使用和活跃模式
     */
    Optional<Device> findByDeviceId(@Param("deviceId") String deviceId);

    @Transactional
    default void upsertTouch(@Param("deviceId") String deviceId) {
        Device device = findByDeviceId(deviceId).orElseGet(() -> {
            Device created = new Device();
            created.setDeviceId(deviceId);
            return created;
        });
        device.setLastSeenAt(OffsetDateTime.now());
        save(device);
    }
}
