package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.Scene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 场景数据访问层 Repository
 *
 * <p>Repository职责说明：
 * 负责学习场景的查询和管理，为用户提供多样化的口语练习环境。
 * 支持按用户画像和分类筛选场景，实现个性化的学习内容推荐。
 * 场景是练习内容的载体，定义了用户练习时的语境和目标。
 *
 * <p>数据完整性保证：
 * - 使用JPA注解确保数据约束关系
 * - NULL值处理支持通用场景和专属场景
 * - 排序规则保证查询结果的稳定性
 *
 * <p>性能优化策略：
 * - 使用JPQL优化查询逻辑
 * - 分页查询避免大数据集问题
 * - 合理使用索引加速分类查询
 * - 缓存常用场景数据
 *
 * <p>与业务逻辑的关联：
 * - 与SceneService配合实现场景管理
 * - 为DailyTopicService提供场景分类支持
 * - 支持用户兴趣和偏好推荐
 * - 实现学习路径的动态调整
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public interface SceneRepository extends JpaRepository<Scene, Long> {

    /**
     * 根据用户画像查询场景列表
     *
     * <p>方法说明：
     * 核心查询方法，获取适合特定用户画像的所有场景。
     * 支持两种类型的场景：
     * 1. 通用场景：targetPersona为NULL，适用于所有用户
     * 2. 专属场景：targetPersona匹配特定画像，提供针对性内容
     *
     * <p>查询逻辑：
     * WHERE s.targetPersona IS NULL OR s.targetPersona = :persona
     * 这个条件确保返回所有通用场景和匹配指定画像的专属场景。
     *
     * <p>参数说明：
     * @param persona 用户画像枚举值，如BEGINNER、INTERMEDIATE、ADVANCED
     *                用于筛选适合用户当前水平的练习场景
     *                NULL值表示查询所有画像的场景（主要用于测试）
     *
     * <p>返回值说明：
     * @return List<Scene> 场景列表，按分类和ID排序
     *         包含完整的场景实体对象，可访问所有字段
     *         排序规则：category升序，id升序，保证分类有序
     *
     * <p>排序机制：
     * ORDER BY s.category ASC, s.id ASC
     * - 首先按分类名称排序，使同类场景聚集
     * - 然后按ID排序，保证同一分类内场景顺序稳定
     * 这种排序有利于前端展示和用户浏览
     *
     * <p>业务场景说明：
     * 1. 用户注册后：展示适合其画像的场景列表
     * 2. 场景切换：根据用户选择动态加载场景
     * 3. 学习推荐：基于用户画像推荐合适的练习场景
     * 4. 内容探索：允许用户浏览所有可用场景
     *
     * <p>性能考虑：
     * - 确保 targetPersona 和 category 字段有索引
     * - 考虑添加缓存层，因为场景数据变化不频繁
     * - 大数据量时注意内存使用，可考虑分页
     *
     * <p>异常处理：
     * - 方法本身不抛出异常，由JPA框架处理
     * - 调用方需要处理可能的数据库连接异常
     * - 注意空值场景的处理逻辑
     */
    @Query("""
        SELECT s
        FROM Scene s
        WHERE s.targetPersona IS NULL OR s.targetPersona = :persona
        ORDER BY s.category ASC, s.id ASC
    """)
    List<Scene> findByPersona(@Param("persona") UserPersona persona);

    /**
     * 根据用户画像和分类分页查询场景
     *
     * <p>方法说明：
     * 增强版的场景查询方法，支持按分类筛选和分页功能。
     * 在画像筛选的基础上增加了分类过滤，提供更精确的内容检索。
     * 分页机制适合移动端和大数据量的场景展示。
     *
     * <p>查询逻辑：
     * WHERE条件包含两部分：
     * 1. 画像筛选：(s.targetPersona IS NULL OR s.targetPersona = :persona)
     * 2. 分类过滤：(:category IS NULL OR s.category = :category)
     * 分类参数为NULL时忽略分类过滤，查询所有分类
     *
     * <p>参数说明：
     * @param persona 用户画像，决定场景的难度和风格
     * @param sceneCategory 场景分类名称，如"日常对话"、"商务英语"等
     *                     允许NULL值，表示不过滤分类
     * @param pageable 分页参数，包含页码、页面大小、排序规则等
     *                 支持自定义排序，但会覆盖默认的category/id排序
     *
     * <p>返回值说明：
     * @return Page<Scene> 分页场景结果对象
     *         包含场景列表和分页元信息（总数、总页数、是否有下一页等）
     *         便于前端实现分页导航和加载更多功能
     *
     * <p>分页优势：
     * 1. 减少内存占用：每次只加载一页数据
     * 2. 提升响应速度：查询数据量减少
     * 3. 改善用户体验：渐进式加载，避免等待
     * 4. 适配移动端：适应小屏幕设备的数据展示
     *
     * <p>业务场景应用：
     * 1. 场景分类浏览：用户按分类查看场景
     * 2. 场景搜索结果：搜索结果的分页展示
     * 3. 个性化推荐：推荐场景的分页加载
     * 4. 后台管理：管理员分页查看场景数据
     *
     * <p>性能优化建议：
     * - 确保 (targetPersona, category) 复合索引
     * - 分页大小根据业务需求设置（通常10-20条）
     * - 考虑预加载常用分类的场景数据
     * - 监控分页查询的执行效率
     *
     * <p>使用注意事项：
     * - 分类参数为NULL时返回所有分类的场景
     * - 分页参数会影响默认排序规则
     * - 大偏移量分页性能较差，建议使用游标分页
     * - 注意并发场景下的数据一致性
     *
     * <p>扩展性考虑：
     * - 可扩展支持多分类筛选（AND/OR逻辑）
     * - 可添加难度、时长等筛选条件
     * - 支持场景标签和关键词搜索
     * - 可与用户偏好算法结合实现智能推荐
     */
    @Query("""
        SELECT s FROM Scene s
        WHERE (s.targetPersona IS NULL OR s.targetPersona = :persona)
          AND (:category IS NULL OR s.category = :category)
        ORDER BY s.category ASC, s.id ASC
    """)
    Page<Scene> findByPersonaAndCategory(
            @Param("persona") UserPersona persona,
            @Param("category") String category,
            Pageable pageable
    );
}