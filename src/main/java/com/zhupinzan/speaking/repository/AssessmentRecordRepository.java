package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评估记录数据访问层 Repository
 *
 * <p>Repository职责说明：
 * 负责用户口语评估记录的数据库访问操作，是系统的核心数据仓库。
 * 管理用户每次口语练习的详细评估数据，包括音频文件、评分结果、反馈建议等，
 * 为用户成长轨迹分析和个性化推荐提供数据支撑。
 *
 * <p>数据完整性保证：
 * - 使用复合主键确保记录唯一性
 * - 通过deviceId + persona组合实现数据隔离
 * - 采用自定义SQL保证数据一致性和原子性
 * - 使用Projection和DTO优化数据传输安全性
 *
 * <p>性能优化策略：
 * - 使用Projection减少数据传输量
 * - 分页查询避免大数据集问题
 * - 索引优化：deviceId、persona、createdAt字段
 * - 聚合查询使用数据库原生计算
 *
 * <p>与业务逻辑的关联：
 * - 与GrowthHistoryService配合实现用户成长展示
 * - 为RadarChartService提供统计数据支持
 * - 支持个性化学习路径推荐
 * - 实现学习数据导出功能
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {

    /**
     * 列表页视图投影接口
     *
     * <p>设计目的：
     * 为用户成长历史列表提供极度轻量化的数据结构，只包含列表展示所需的核心字段，
     * 避免加载大文本字段（prompt、transcript、metrics），提高列表加载性能。
     *
     * <p>字段说明：
     * @see #getId() 评估记录ID，用于详情页跳转
     * @see #getCreatedAt() 创建时间，用于排序和显示
     * @see #getOverallScore() 总体评分，用于星星展示
     * @see #getFluency() 流利度评分，雷达图维度
     * @see #getCompleteness() 完整度评分，雷达图维度
     * @see #getRelevance() 相关性评分，雷达图维度
     * @see #getScene() 场景名称，用于分类展示
     */
    interface GrowthHistoryView {
        Long getId();

        OffsetDateTime getCreatedAt();

        Double getOverallScore();

        // 仅返回核心三维分
        Double getFluency();

        Double getCompleteness();

        Double getRelevance();

        // 🔧 新增：返回场景/题目
        String getScene();
    }

    /**
     * 雷达图统计数据DTO接口
     *
     * <p>设计目的：
     * 为雷达图可视化提供聚合统计数据，包含三个维度的平均值和总记录数。
     * 实现数据在前端的高效渲染，避免客户端二次计算。
     *
     * <p>业务场景：
     * - 用户个人主页展示近期的能力发展趋势
     * - 学习报告中的能力分析图表
     * - 个性化学习路径的数据依据
     */
    interface RadarStatsDTO {
        Double getAvgFluency();

        Double getAvgCompleteness();

        Double getAvgRelevance();

        Long getTotalCount();
    }

    /**
     * 详情页轻量视图投影接口
     *
     * <p>设计目的：
     * 为评估详情页提供必要信息的同时，避免加载大字段数据（transcript、metrics、feedback），
     * 在详情和性能之间取得平衡。
     *
     * <p>包含字段：
     * - 基本信息：ID、创建时间、更新时间
     * - 评估配置：模式、画像、场景、提示词
     * - 评分结果：总分及三维分
     * - 音频链接：用于播放回放
     */
    interface GrowthDetailView {
        Long getId();

        OffsetDateTime getCreatedAt();

        OffsetDateTime getUpdatedAt();

        AssessmentMode getMode();

        UserPersona getPersona();

        String getScene();

        String getPrompt();

        Double getOverallScore();

        Double getFluency();

        Double getCompleteness();

        Double getRelevance();

        String getAudioUrl();
    }

    /**
     * 查询用户设备的评估历史列表
     *
     * <p>方法说明：
     * 根据设备ID和用户画像查询该设备下特定画像的所有评估记录，
     * 用于用户在个人主页查看自己的学习历史。
     *
     * <p>安全机制：
     * 自动按deviceId + persona进行数据隔离，确保用户只能看到自己的评估记录。
     * 这是系统安全性的基础保障。
     *
     * <p>参数说明：
     * @param deviceId 设备唯一标识符，用于数据隔离
     * @param persona 用户画像类型，决定显示哪类评估记录
     * @param pageable 分页参数，控制返回的数据量和页码
     *
     * <p>返回值：
     * @return List<GrowthHistoryView> 轻量化的评估记录列表，按创建时间倒序排列
     */
    List<GrowthHistoryView> findByDeviceIdAndPersona(
            String deviceId, UserPersona persona, Pageable pageable);

    /**
     * 带时间过滤的评估历史查询
     *
     * <p>业务场景：
     * 用户可能只想查看特定时间段内的评估记录，如"本周"、"本月"或自定义时间段。
     * 此方法支持起始时间过滤，配合前端时间选择器使用。
     *
     * <p>性能考虑：
     * 使用createdAtAfter条件时，确保该字段有索引以避免全表扫描。
     * 大数据量场景下，建议结合分页使用。
     */
    List<GrowthHistoryView> findByDeviceIdAndPersonaAndCreatedAtAfter(
            String deviceId, UserPersona persona, OffsetDateTime from, Pageable pageable);

    /**
     * 分页版的评估历史列表查询
     *
     * <p>用途：
     * 用于需要精确分页的场景，如移动端分页加载或大数据量的前端展示。
     * 返回Page对象包含分页元信息（总数、页码、每页大小等）。
     */
    Page<GrowthHistoryView> findPageByDeviceIdAndPersona(
            String deviceId, UserPersona persona, Pageable pageable);

    /**
     * 带时间过滤的分页查询
     *
     * <p>组合场景：
     * 同时满足分页和时间过滤的需求，适用于需要时间范围限制的分页展示，
     * 如"最近30天的评估记录"的分页查询。
     */
    Page<GrowthHistoryView> findPageByDeviceIdAndPersonaAndCreatedAtAfter(
            String deviceId, UserPersona persona, OffsetDateTime from, Pageable pageable);

    /**
     * 根据ID查询评估记录详情
     *
     * <p>安全增强：
     * 在findById基础上增加deviceId参数，确保用户只能查询自己设备上的记录，
     * 防止越权访问其他用户的评估数据。
     *
     * <p>返回值处理：
     * 使用Optional包装，调用方需要处理存在性判断，避免NullPointerException。
     */
    Optional<AssessmentRecord> findByIdAndDeviceId(Long id, String deviceId);

    /**
     * 轻量化详情查询
     *
     * <p>设计理念：
     * 与findByIdAndDeviceId不同，此方法返回的是GrowthDetailView投影，
     * 避免加载大文本字段，提高详情页加载速度。
     *
     * <p>适用场景：
     * - 详情页只需要显示基本信息和评分结果
     * - 不需要查看详细的评估过程数据
     * - 对加载速度要求较高的场景
     */
    Optional<GrowthDetailView> findDetailViewByIdAndDeviceId(Long id, String deviceId);

    /**
     * 雷达图统计查询
     *
     * <p>SQL说明：
     * 使用原生SQL查询，计算指定时间段内三个维度的平均值和总记录数。
     * WHERE条件确保数据隔离和时间范围过滤。
     *
     * <p>参数说明：
     * @param deviceId 设备标识，确保数据隔离
     * @param persona 用户画像，统计特定画像的数据
     * @param from 起始时间，统计从此刻到现在的数据
     *
     * <p>返回结果：
     * 包含三个维度平均值和总记录数的统计数据对象
     *
     * <p>性能优化：
     * - 使用数据库聚合函数，减少数据传输量
     * - 确保deviceId和createdAt字段有复合索引
     * - 避免在Java层进行大量计算
     */
    @Query("""
                SELECT
                    AVG(r.fluency) as avgFluency,
                    AVG(r.completeness) as avgCompleteness,
                    AVG(r.relevance) as avgRelevance,
                    COUNT(r) as totalCount
                FROM AssessmentRecord r
                WHERE r.deviceId = :deviceId
                  AND r.persona = :persona
                  AND r.createdAt >= :from
            """)
    RadarStatsDTO findRadarStats(
            @Param("deviceId") String deviceId,
            @Param("persona") UserPersona persona,
            @Param("from") OffsetDateTime from);

    /**
     * 兼容旧接口：获取某设备和画像的评估记录
     *
     * <p>用途说明：
     * 为保持向后兼容性，保留此方法用于历史记录功能。
     * 返回完整的AssessmentRecord实体，包含所有字段。
     *
     * <p>使用场景：
     * - 需要访问大文本字段（如transcript）的场景
     * - 数据导出功能
     * - 后台管理系统查看详细记录
     *
     * <p>注意事项：
     * 此方法返回完整实体，数据量较大，建议谨慎使用。
     * 新代码优先使用GrowthHistoryView投影。
     */
    List<AssessmentRecord> findByDeviceIdAndPersonaOrderByCreatedAtDesc(String deviceId, UserPersona persona);

    /**
     * 兼容旧接口：获取某设备的所有记录
     *
     * <p>业务场景：
     * 用于用户个人主页的总统计展示，显示用户在所有画像上的总体学习情况。
     * 配合前端统计组件展示用户的总体学习时长、练习次数等指标。
     *
     * <p>数据统计：
     * - 学习总时长：通过duration字段的累加计算
     * - 练习次数：记录总数
     * - 能力趋势：需要进一步的数据聚合
     */
    List<AssessmentRecord> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
}
