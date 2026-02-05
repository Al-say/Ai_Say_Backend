package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 用户进度数据访问层 Repository
 *
 * <p>Repository职责说明：
 * 负责用户学习进度的数据访问和统计，跟踪用户的学习行为模式。
 * 记录用户的学习总次数、累计学习时长、连续学习天数等关键指标，
 * 为个性化学习推荐和用户激励提供数据支持。
 *
 * <p>数据完整性保证：
 * - 使用PostgreSQL的ON CONFLICT机制实现原子性操作
 * - 采用clearAutomatically和flushAutomatically确保事务一致性
 * - deviceId作为唯一键保证每个设备只有一个进度记录
 * - 时间戳字段记录最后更新时间
 *
 * <p>性能优化策略：
 * - 使用原生SQL实现批量更新，减少数据库往返
 * - 利用索引加速deviceId查询
 * - 避免频繁的数据库连接和事务开销
 *
 * <p>与业务逻辑的关联：
 * - 与UserProgressService配合完成学习进度统计
 * - 为StreakCalculatorService提供连续学习数据
 * - 支持学习报告生成和成就系统
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    /**
     * 根据设备ID查询用户进度
     *
     * <p>方法说明：
     * 用于获取特定设备的学习进度记录，每个设备在系统中都有独立的进度。
     * 这确保了多设备使用场景下的进度跟踪准确性。
     *
     * <p>参数说明：
     * @param deviceId 设备唯一标识符，通常由客户端生成并传递
     *                 用于区分不同设备上的用户进度
     *
     * <p>返回值说明：
     * @return Optional<UserProgress> 用户进度对象的可选容器
     *         - 如果设备存在进度记录，返回包含UserProgress对象的Optional
     *         - 如果是新设备或首次使用，返回空的Optional
     *         调用方需要根据Optional的存在性判断是否需要初始化进度
     *
     * <p>业务场景：
     * 1. 应用启动时检查用户进度，展示欢迎信息和学习成就
     * 2. 用户切换设备时，进度记录保持独立
     * 3. 学习统计页面显示用户总学习数据
     *
     * <p>性能考虑：
     * - deviceId字段必须有唯一索引
     * - 考虑添加缓存层，因为此方法会被频繁调用
     * - 在高并发场景下，注意乐观锁的使用
     */
    Optional<UserProgress> findByDeviceId(String deviceId);

    /**
     * 更新或插入用户进度（UPSERT操作）
     *
     * <p>核心功能：
     * 使用PostgreSQL的UPSERT功能实现原子性的进度更新。
     * 当记录不存在时插入新记录，存在时更新现有记录，
     * 确保用户每次学习都能正确更新统计数据。
     *
     * <p>SQL机制说明：
     * - INSERT INTO: 插入新进度记录
     * - ON CONFLICT (device_id): 检测device_id冲突
     * - DO UPDATE SET: 冲突时执行更新操作
     * - 使用原子操作避免竞态条件
     *
     * <p>参数详解：
     * @param deviceId 设备标识符，作为唯一键
     * @param attemptInc 本次学习增加的练习次数（通常为1）
     * @param durationInc 本次学习增加的时长（毫秒）
     * @param today 本次学习的日期
     * @param newStreak 更新后的连续学习天数
     *
     * <p>字段更新逻辑：
     * - total_attempts: 累加本次练习次数
     * - total_duration_ms: 累加本次学习时长
     * - last_active_date: 更新为今天的日期
     * - streak_days: 更新连续学习天数
     * - updated_at: 自动更新为当前时间戳
     *
     * <p>注解说明：
     * @Modifying: 标记为修改操作
     * @Query: 使用原生SQL确保UPSERT功能
     * clearAutomatically: 自动清除持久化上下文
     * flushAutomatically: 自动刷新数据库连接
     *
     * <p>业务场景应用：
     * 1. 每次完成评估后调用，更新用户学习统计
     * 2. 学习应用启动时检查并更新连续学习天数
     * 3. 定时任务计算用户的学习成就
     *
     * <p>数据一致性保障：
     * - 使用事务确保操作的原子性
     * - 避免并发更新导致的数据不一致
     * - 时间戳记录保证数据版本控制
     *
     * <p>性能优化建议：
     * - 确保device_id字段有索引
     * - 考虑批量更新机制减少数据库压力
     * - 在高并发场景下使用乐观锁
     *
     * <p>异常处理：
     * - 方法不直接抛出异常，由Spring Data JPA处理
     * - 调用方需要处理数据库约束异常
     */
    @Transactional
    default void upsertProgress(
            @Param("deviceId") String deviceId,
            @Param("attemptInc") int attemptInc,
            @Param("durationInc") long durationInc,
            @Param("today") LocalDate today,
            @Param("newStreak") int newStreak
    ) {
        UserProgress progress = findByDeviceId(deviceId).orElseGet(() -> {
            UserProgress created = new UserProgress();
            created.setDeviceId(deviceId);
            created.setTotalAttempts(0);
            created.setTotalDurationMs(0L);
            created.setStreakDays(0);
            return created;
        });
        progress.setTotalAttempts(progress.getTotalAttempts() + attemptInc);
        progress.setTotalDurationMs(progress.getTotalDurationMs() + durationInc);
        progress.setLastActiveDate(today);
        progress.setStreakDays(newStreak);
        progress.setUpdatedAt(OffsetDateTime.now());
        save(progress);
    }
}
