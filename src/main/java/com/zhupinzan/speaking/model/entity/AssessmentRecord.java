package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.UserPersona;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 评估记录实体类 - 核心业务实体
 *
 * 【实体业务含义】
 * AssessmentRecord 是AI口语练习应用的核心业务实体，用于记录用户每次口语练习的评估结果。
 * 该实体承载了用户语言学习的完整数据链路，从练习内容到评估反馈，构成了用户学习数据的基础。
 *
 * 【核心作用】
 * - 记录用户的口语练习行为数据
 * - 存储AI评估的量化分数和详细反馈
 * - 支持多模式评估（基础练习、随机模式等）
 * - 提供用户角色场景化评估能力
 * - 为学习分析和个性化推荐提供数据基础
 *
 * 【设计特点】
 * - 采用混合存储策略：核心指标扁平化存储，详细数据JSONB存储
 * - 统一的时间戳管理，确保数据一致性
 * - 自动同步核心指标到JSON字段，保证数据完整性
 */
@Entity
@Table(name = "assessment_record")
@Data
public class AssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /**
     * 主键ID - 自增长
     * 【业务含义】评估记录的唯一标识符
     * 【数据类型】Long - 支持大规模数据存储
     * 【业务规则】自动生成，无需手动设置
     */
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    /**
     * 设备标识符
     * 【业务含义】关联到具体用户设备，支持多设备切换场景
     * 【数据类型】String(64) - UUID或设备指纹
     * 【业务规则】必填字段，确保记录归属明确
     * 【关联关系】关联Device实体
     */
    private String deviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    /**
     * 用户ID
     * 【业务含义】关联到用户账户，支持登录后数据回填
     * 【数据类型】Long - 用户主键
     * 【业务规则】可为空，未登录用户为null
     * 【关联关系】关联UserAccount实体
     */
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    /**
     * 任务ID
     * 【业务含义】关联到异步评估任务，用于数据溯源
     * 【数据类型】String - 任务UUID
     * 【业务规则】可为空，同步评估为null
     * 【关联关系】关联EvaluationTask实体
     */
    private EvaluationTask task;

    @Column(name = "created_at", nullable = false)
    /**
     * 记录创建时间
     * 【业务含义】评估产生的初始时间戳
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】自动填充，不可为空
     * 【用途】用于数据追溯和学习趋势分析
     */
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // ✅ 补齐
    /**
     * 记录更新时间
     * 【业务含义】评估结果或相关数据最后修改时间
     * 【数据类型】OffsetDateTime - 支持时区
     * 【业务规则】自动更新，确保数据时效性
     * 【用途】用于缓存失效和数据同步判断
     */
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    /**
     * 评估模式
     * 【业务含义】定义评估的具体类型和规则
     * 【数据类型】AssessmentMode枚举 - 映射为字符串存储
     * 【业务规则】必填字段，支持基础练习(random)、其他模式扩展
     * 【用途】影响评估算法和评分标准
     * 【设计】使用枚举类型保证数据一致性
     */
    private AssessmentMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64) // ✅ 扩容：原来32位，现在64位
    /**
     * 用户角色画像
     * 【业务含义】定义用户在评估场景中的身份定位
     * 【数据类型】UserPersona枚举 - 映射为字符串存储
     * 【业务规则】必填字段，影响AI交互风格和评估维度
     * 【用途】提供场景化、个性化的评估体验
     * 【示例】学生、职场人士、语言爱好者等不同角色
     */
    private UserPersona persona;

    @Column(nullable = false, columnDefinition = "TEXT") // ✅ 扩容：原来32位，现在支持长文本
    /**
     * 使用场景
     * 【业务含义】定义评估的具体应用场景
     * 【数据类型】TEXT - 支持长文本存储
     * 【业务规则】必填字段，默认值"practice"
     * 【用途】区分练习、测试、正式评估等不同场景
     * 【设计】支持未来多场景扩展，如考试模拟、面试练习等
     */
    private String scene = "practice";

    @Column(columnDefinition = "TEXT")
    /**
     * 评估提示/题目
     * 【业务含义】用户需要完成的口语练习内容
     * 【数据类型】TEXT - 支持长文本存储
     * 【业务规则】可为空，根据评估模式决定是否需要
     * 【用途】记录练习的具体任务和内容要求
     * 【示例】"请描述你最喜欢的旅行目的地"
     */
    private String prompt;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setPersona(UserPersona persona) {
        this.persona = persona;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public void setMode(AssessmentMode mode) {
        this.mode = mode;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    @Column(name = "overall_score")
    /**
     * 总体评分
     * 【业务含义】AI对用户口语表现的整体评价分数
     * 【数据类型】Double - 0-100分的浮点数
     * 【业务规则】可为空，某些评估模式可能不提供总分
     * 【用途】快速了解整体表现，用于排行榜和成就系统
     */
    private Double overallScore;

    // 扁平列 - 核心评分维度
    /**
     * 流利度评分
     * 【业务含义】评估用户口语表达的流畅程度
     * 【数据类型】Double - 0-100分
     * 【业务规则】可与metrics字段同步存储
     * 【用途】专门评估语速、停顿、流利程度等
     */
    private Double fluency;

    /**
     * 完整性评分
     * 【业务含义】评估用户回答内容的完整度
     * 【数据类型】Double - 0-100分
     * 【业务规则】可与metrics字段同步存储
     * 【用途】评估是否覆盖所有要点，信息完整程度
     */
    private Double completeness;

    /**
     * 相关性评分
     * 【业务含义】评估用户回答与题目的相关程度
     * 【数据类型】Double - 0-100分
     * 【业务规则】可与metrics字段同步存储
     * 【用途】评估回答的切题程度和内容相关性
     */
    private Double relevance;

    // JSONB - 扩展评估数据和反馈
    /**
     * 扩展评估指标
     * 【业务含义】存储除核心指标外的详细评估数据
     * 【数据类型】JSONB - PostgreSQL原生JSON二进制格式
     * 【业务规则】必填字段，默认为空HashMap
     * 【JSONB使用场景】
     *   - 存储详细的语言特征分析（语速、音调、发音准确度等）
     *   - 记录时间序列的评估数据
     *   - 存储自定义评估维度的分数
     *   - 支持评估模型的版本控制和扩展
     * 【设计优势】JSONB支持索引查询，便于数据分析和统计
     */
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metrics = new HashMap<>();

    /**
     * 详细反馈信息
     * 【业务含义】AI生成的个性化学习建议和改进方向
     * 【数据类型】JSONB - PostgreSQL原生JSON二进制格式
     * 【业务规则】必填字段，默认为空HashMap
     * 【JSONB使用场景】
     *   - 存储具体的改进建议和练习方向
     *   - 记录错误分析和纠正建议
     *   - 提供个性化的学习资源推荐
     *   - 支持多语言反馈内容
     * 【设计考虑】使用get/set方法返回可修改副本，防止外部修改影响数据一致性
     */
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> feedback = new HashMap<>();

    // ✅ 修复：返回可修改的副本
    /**
     * 获取评估指标的可修改副本
     * 【设计考虑】防止外部代码直接修改内部Map，保证数据封装性
     * 【返回值】新的HashMap实例，包含所有指标数据
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics != null ? new HashMap<>(metrics) : new HashMap<>();
    }

    // ✅ 修复：返回可修改的副本
    /**
     * 获取反馈信息可修改副本
     * 【设计考虑】同getMetrics，确保数据封装和线程安全
     */
    public Map<String, Object> getFeedback() {
        return new HashMap<>(feedback);
    }

    public void setFeedback(Map<String, Object> feedback) {
        this.feedback = feedback != null ? new HashMap<>(feedback) : new HashMap<>();
    }

    @Column(name = "audio_url")
    /**
     * 音频文件URL
     * 【业务含义】用户口语练习录音的访问地址
     * 【数据类型】String - 完整的URL路径
     * 【业务规则】可为空，取决于评估模式和存储策略
     * 【用途】支持音频回放和二次评估
     * 【存储】可能使用云存储服务（如AWS S3、阿里云OSS等）
     */
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    /**
     * 语音转文本结果
     * 【业务含义】将用户口语录音转换成的文本内容
     * 【数据类型】TEXT - 支持长文本存储
     * 【业务规则】可为空，依赖语音识别服务
     * 【用途】文本化分析用户表达内容，便于深度评估
     * 【技术栈】可能集成第三方ASR服务（如科大讯飞、阿里云等）
     */
    private String transcript;

    // ✅ 生命周期钩子：统一维护时间与数据一致性
    /**
     * 实体创建前触发
     * 【功能】统一处理创建时间和更新时间
     * 【数据同步】自动将核心指标同步到JSON字段
     * 【业务价值】确保数据一致性和完整性
     */
    @PrePersist
    protected void onCreate() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        syncCoreMetrics();
    }

    /**
     * 实体更新前触发
     * 【功能】统一处理更新时间戳
     * 【数据同步】确保扁平化的核心指标与JSON字段保持一致
     * 【设计原则】在应用层自动维护，避免手动更新遗漏
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(); // ✅ 应用层自动更新
        syncCoreMetrics();
    }

    /**
     * 同步核心指标到JSON字段
     * 【业务逻辑】将扁平化的核心评分维度同步到metrics JSON字段
     * 【数据一致性】确保两种存储方式的数据同步
     * 【扩展性】未来可在JSON字段中添加更多评估维度
     * 【性能优化】针对高频查询的维度进行扁平化存储，提升查询性能
     */
    private void syncCoreMetrics() {
        if (metrics == null) metrics = new HashMap<>();
        if (fluency != null) metrics.put("fluency", fluency);
        if (completeness != null) metrics.put("completeness", completeness);
        if (relevance != null) metrics.put("relevance", relevance);
    }
}
