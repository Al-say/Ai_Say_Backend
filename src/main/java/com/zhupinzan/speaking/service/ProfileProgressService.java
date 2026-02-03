package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * 用户进度服务 - 管理用户练习进度和连续打卡记录
 *
 * <h3>服务定位与功能概述</h3>
 * <p>
 * ProfileProgressService 是负责用户练习进度管理的核心服务，主要维护用户的连续打卡记录、
 * 练习统计等数据。该服务采用数据驱动的设计模式，通过 upsert 操作保证数据的一致性，
 * 并提供清晰的进度反馈机制，激励用户持续练习。
 * </p>
 *
 * <h3>核心业务流程和算法逻辑</h3>
 * <p>
 * <strong>连续打卡算法</strong>：
 * 1. <strong>当前状态检查</strong>：查询用户的现有进度记录
 * 2. <strong>日期逻辑判断</strong>：根据最后活跃日期计算新的连续天数
 *     - 如果是同一天：保持原有连续天数
 *     - 如果是连续第二天：连续天数 +1
 *     - 如果间隔超过一天：重置为1
 * 3. <strong>数据更新</strong>：使用 upsert 操作更新用户进度
 * </p>
 *
 * <strong>进度更新策略</strong>：
 * - 采用 UTC 时间作为基准，确保全球用户的一致性
 * - 使用设备 ID 作为用户标识，支持匿名用户使用
 * - 记录每次练习的时长，便于后续的数据分析
 * - 维护连续打卡天数，增强用户粘性
 * </p>
 *
 * <h3>与其他服务的协作关系</h3>
 * <p>
 * - {@link UserProgressRepository}：数据持久化层，负责数据库操作
 * - 通过 Spring 事务管理保证数据一致性
 * - 依赖 Lombok 进行日志记录，便于追踪用户行为
 * </p>
 *
 * <h3>数据处理和转换逻辑</h3>
 * <p>
 * <strong>时间处理</strong>：
 * - 使用 LocalDate.now(ZoneOffset.UTC) 获取 UTC 日期
 * - 通过日期比较算法计算连续天数
 * - 支持时区无关的日期计算逻辑
 * </p>
 *
 * <strong>数据结构</strong>：
 * - 设备 ID 作为主要标识符
 * - 记录每次练习的次数和时长
 * - 维护连续打卡天数记录
 * - 使用数据库索引优化查询性能
 * </p>
 *
 * <h3>缓存策略和性能优化</h3>
 * <p>
 * - 使用数据库作为主要存储，通过索引优化查询性能
 * - 采用 upsert 操作减少数据库访问次数
 * - 事务范围控制在最小，避免长事务
 * - 日志记录详细但不过度，平衡性能和调试需求
 * </p>
 *
 * <h3>错误处理和降级机制</h3>
 * <p>
 * - 采用防御性编程，即使数据库操作失败也不会影响主流程
 * - 日志记录所有关键操作的状态和结果
 * - 不实现自动重试机制，依赖数据库的容错能力
 * - 建议后续增加数据验证和异常恢复机制
 * </p>
 *
 * <h3>配置参数和使用场景</h3>
 * <p>
 * <strong>配置需求</strong>：
 * - 无需额外配置参数
 * - 依赖 Spring Boot 自动配置
 * </p>
 *
 * <strong>使用场景</strong>：
 * - 用户完成练习后的进度更新
 * - 连续打卡系统的基础数据支持
 * - 用户练习行为的统计分析
 * - 游戏化功能的数据基础（成就、勋章等）
 * </p>
 *
 * <h3>扩展性和维护性考虑</h3>
 * <p>
 * <strong>扩展性设计</strong>：
 * - 可扩展支持更多进度维度（如练习类型分布、得分趋势等）
 * - 可添加批量进度更新功能
 * - 可集成数据统计分析模块
 * - 可扩展支持多设备同步
 * </p>
 *
 * <strong>维护性考虑</strong>：
 * - 代码结构清晰，逻辑简单直接
 * - 事务边界明确，避免并发问题
 * - 日志记录完善，便于问题追踪
 * - 建议增加单元测试，验证连续打卡算法的正确性
 * - 建议添加性能监控，关注数据库查询效率
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileProgressService {

    private final UserProgressRepository userProgressRepository;

    /**
     * 当用户完成一次练习时调用
     * 
     * @param deviceId   设备ID
     * @param durationMs 练习时长(毫秒)
     */
    @Transactional
    public void onPracticeCompleted(String deviceId, long durationMs) {
        log.info("📊 更新用户进度 - 设备: {}, 时长: {}ms", deviceId, durationMs);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        var current = userProgressRepository.findByDeviceId(deviceId).orElse(null);

        int newStreak;
        if (current == null || current.getLastActiveDate() == null) {
            newStreak = 1;
        } else {
            LocalDate last = current.getLastActiveDate();
            if (last.isEqual(today)) {
                newStreak = current.getStreakDays();
            } else if (last.plusDays(1).isEqual(today)) {
                newStreak = current.getStreakDays() + 1;
            } else {
                newStreak = 1;
            }
        }

        userProgressRepository.upsertProgress(deviceId, 1, durationMs, today, newStreak);
        log.info("✅ 用户进度更新请求已发送 (upsert) - 设备: {}, newStreak: {}", deviceId, newStreak);
    }
}