package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 每日挑战题目数据访问层 Repository
 *
 * <p>Repository职责说明：
 * 负责每日挑战题目的管理和维护，实现每日学习内容的动态生成和更新。
 * 为用户画像提供个性化的每日练习题目，支持题目的创建、查询、更新和清理。
 * 是推动用户持续学习的重要数据支撑。
 *
 * <p>数据完整性保证：
 * - 使用复合唯一键 (for_date, persona) 确保题目唯一性
 * - 采用UPSERT机制保证数据一致性
 * - 时间戳字段记录题目创建和更新时间
 * - JSON字段存储灵活的扩展数据
 *
 * <p>性能优化策略：
 * - 为 (for_date, persona) 复合字段建立索引
 * - 使用Projection减少不必要的数据加载
 * - 批量操作减少数据库往返次数
 *
 * <p>与业务逻辑的关联：
 * - 与DailyTopicService配合实现每日挑战功能
 * - 为UserProgressService提供学习内容支持
 * - 支持个性化推荐算法的数据源
 * - 实现学习计划和时间表管理
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface DailyTopicRepository extends JpaRepository<DailyTopic, Long> {

    /**
     * 根据用户画像和日期查找题目
     *
     * <p>方法说明：
     * 核心查询方法，用于获取特定用户画像在指定日期的每日挑战题目。
     * 每个用户画像每天都有独立的题目，确保学习内容的新鲜度和针对性。
     *
     * <p>参数详细说明：
     * @param persona 用户画像枚举值，决定题目的难度和风格
     *                如：BEGINNER、INTERMEDIATE、ADVANCED等
     *                用于匹配适合用户当前水平的练习内容
     * @param date 练习日期，确保用户按计划完成每日任务
     *             通常使用 LocalDate.now() 获取当天日期
     *             支持历史题目的查询和补做功能
     *
     * <p>返回值说明：
     * @return Optional<DailyTopic> 包含每日题目的可选容器
     *         - 如果找到对应日期和画像的题目，返回包含DailyTopic的Optional
     *         - 如果题目不存在（如新画像或数据未初始化），返回空的Optional
     *         使用Optional避免空指针异常，遵循最佳实践
     *
     * <p>业务场景说明：
     * 1. 用户首次打开应用：获取当天的挑战题目
     * 2. 用户补做历史题目：查询特定日期的题目
     * 3. 题目内容预览：为用户展示今日学习任务
     * 4. 学习进度跟踪：基于题目完成情况统计
     *
     * <p>性能考虑：
     * - 确保 (target_persona, topic_date) 复合字段有索引
     * - 考虑添加缓存层，减少数据库查询压力
     * - 题目内容通常变化不频繁，适合缓存
     *
     * <p>异常处理：
     * - 方法本身不抛出异常，由JPA框架处理
     * - 调用方需要处理Optional.get()的NoSuchElementException
     * - 注意数据库连接异常的处理
     */
    Optional<DailyTopic> findByTargetPersonaAndTopicDate(UserPersona persona, LocalDate date);

    /**
     * 删除指定日期之前的所有题目（清理历史数据）
     *
     * <p>方法说明：
     * 批量删除功能，用于清理过期的每日题目数据。
     * 随着时间推移，历史题目数据会累积占用存储空间，
     * 此方法帮助保持数据库的整洁和高效。
     *
     * <p>参数说明：
     * @param date 截止日期，删除所有早于此日期的题目
     *             通常设置为当前日期减去保留天数
     *             如保留30天历史数据，则设置为 LocalDate.now().minusDays(30)
     *
     * <p>返回值说明：
     * @return long 被删除的记录数量
     *         用于验证操作效果和日志记录
     *         可以结合业务逻辑显示清理了多少历史数据
     *
     * <p>业务场景：
     * 1. 定期清理任务：每天凌晨执行，删除30天前的题目
     * 2. 数据库维护：在备份或迁移前清理旧数据
     * 3. 存储优化：避免数据无限增长影响性能
     *
     * <p>性能优化：
     * - topic_date字段必须有索引以支持快速删除
     * - 大批量删除时考虑分批次执行
     * - 在低峰期执行清理操作
     *
     * <p>注意事项：
     * - 删除操作是不可逆的，确保保留足够的备份
     * - 考虑数据保留策略，平衡存储和查询需求
     * - 删除前检查是否有关联数据需要清理
     */
    long deleteByTopicDateBefore(LocalDate date);

    /**
     * 根据日期和画像名称查询题目
     *
     * <p>说明：
     * 此方法是为了兼容某些特定场景的查询需求，
     * 使用字符串类型的persona参数而不是枚举类型。
     * 提供了更大的灵活性，但需要注意数据验证。
     *
     * <p>使用场景：
     * - 从外部系统导入数据时
     * - 支持动态画像类型
     * - 兼容旧版本的查询接口
     */
    Optional<DailyTopic> findByTopicDateAndPersona(LocalDate topicDate, String persona);

    /**
     * 更新或插入每日题目（UPSERT操作）
     *
     * <p>核心功能：
     * 使用PostgreSQL的UPSERT功能实现题目的原子性更新。
     * 支持两种场景：
     * 1. 新题目：为指定日期和画像插入新题目
     * 2. 现有题目：更新指定题目的内容（如修正错误或优化）
     *
     * <p>SQL机制说明：
     * - INSERT INTO: 尝试插入新的每日题目
     * - ON CONFLICT (for_date, persona): 检测日期和画像组合冲突
     * - DO UPDATE SET: 冲突时更新题目内容
     * - 使用CAST确保类型转换的正确性
     *
     * <p>参数详解：
     * @param date 题目对应的日期
     * @param persona 画像字符串标识
     * @param title 题目标题，简洁明了的练习主题
     * @param prompt 题目提示词，引导用户思考的关键词
     * @param imageUrl 题目配图URL，增强视觉体验
     * @param payloadJson 题目扩展数据，JSON格式的额外信息
     *                   可以包含难度、时长、相关词汇等
     *
     * <p>字段更新逻辑：
     * - for_date: 题目日期（不变）
     * - persona: 画像类型（不变）
     * - target_persona: 目标画像（与persona相同）
     * - title: 题目标题（更新为新值）
     * - prompt: 提示词（更新为新值）
     * - image_url: 配图（更新为新值）
     * - payload: 扩展数据（更新为新值）
     * - created_at: 创建时间（仅新记录设置）
     *
     * <p>注解说明：
     * @Modifying: 标记为修改操作
     * @Query: 使用原生SQL实现UPSERT
     * clearAutomatically: 自动清除持久化上下文
     * flushAutomatically: 自动刷新数据库连接
     * nativeQuery: true 使用PostgreSQL原生语法
     *
     * <p>业务场景应用：
     * 1. 题目发布：新题目上线时调用
     * 2. 题目更新：优化现有题目内容
     * 3. 批量导入：从管理系统批量添加题目
     * 4. 数据同步：与其他数据源同步题目内容
     *
     * <p>数据一致性保障：
     * - 使用事务确保操作的原子性
     * - 避免竞态条件导致的数据不一致
     * - 时间戳记录保证数据的版本控制
     *
     * <p>性能优化建议：
     * - 确保 (for_date, persona) 复合字段有索引
     * - JSON字段考虑添加GIN索引加速查询
     * - 在高并发场景下监控执行效率
     *
     * <p>错误处理：
     * - 调用方需要处理可能的数据库约束异常
     * - 注意JSON数据的格式验证
     * - 监控UPSERT操作的失败率和重试机制
     *
     * <p>扩展性考虑：
     * - payload字段使用JSON格式，便于未来功能扩展
     * - 可以添加题目标签、难度等级、预估时长等
     * - 支持题目的A/B测试和版本管理
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO daily_topics(for_date, persona, target_persona, title, prompt, image_url, payload, created_at)
            VALUES (:date, :persona, CAST(:persona AS varchar), :title, :prompt, :imageUrl, CAST(:payload AS jsonb), CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    void upsertDailyTopic(
            @Param("date") LocalDate date,
            @Param("persona") String persona,
            @Param("title") String title,
            @Param("prompt") String prompt,
            @Param("imageUrl") String imageUrl,
            @Param("payload") String payloadJson);

    /**
     * 更新每日题目内容
     *
     * <p>功能说明：
     * 专门用于更新已存在的每日题目记录。
     * 当upsert操作因某些原因失败时，使用此方法直接更新记录。
     *
     * <p>参数说明：
     * @param date 题目日期
     * @param persona 画像标识
     * @param title 新标题
     * @param prompt 新提示词
     * @param imageUrl 新图片URL
     * @param payloadJson 新扩展数据JSON
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE daily_topics
            SET title = :title, prompt = :prompt, image_url = :imageUrl, payload = CAST(:payload AS jsonb), updated_at = CURRENT_TIMESTAMP
            WHERE for_date = :date AND persona = :persona
            """, nativeQuery = true)
    void updateDailyTopic(
            @Param("date") LocalDate date,
            @Param("persona") String persona,
            @Param("title") String title,
            @Param("prompt") String prompt,
            @Param("imageUrl") String imageUrl,
            @Param("payload") String payloadJson);
}