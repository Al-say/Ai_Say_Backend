package com.zhupinzan.speaking.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估记录实体类，对应数据库表 assessment_records，用于存储用户口语评估的记录
 */
@Entity
@Table(name = "assessment_records")
public class AssessmentRecord {

    /** 记录ID，主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    /** 用户ID，暂时直接存ID，后续可关联 User 实体 */
    @Column(name = "user_id")
    private Long userId; // 暂时直接存ID，后续可关联 User 实体

    /** 场景ID，对应场景 ID */
    @Column(name = "scenario_id")
    private Long scenarioId; // 对应场景 ID

    // 音频相关 (如果是纯文本评测，这里可为空)
    /** 音频文件URL */
    @Column(name = "audio_file_url")
    private String audioFileUrl;

    /** 音频时长 */
    @Column(name = "audio_duration")
    private Integer audioDuration;

    // --- 核心评分字段 ---
    /** 总分 */
    @Column(name = "score_total")
    private BigDecimal scoreTotal;

    /** 流畅度评分 */
    @Column(name = "score_fluency")
    private BigDecimal scoreFluency;

    /** 完整度/逻辑评分 */
    @Column(name = "score_integrity")
    private BigDecimal scoreIntegrity; // 完整度/逻辑分

    /** 发音评分，文本评测时该项可能为0或不适用 */
    @Column(name = "score_pronunciation")
    private BigDecimal scorePronunciation; // 文本评测时该项可能为0或不适用

    // --- 文本内容 ---
    /** 转录文本 */
    @Column(name = "transcribed_text", columnDefinition = "TEXT")
    private String transcribedText;

    // --- AI 原始分析数据 ---
    // 为了简化开发，我们将 AI 返回的复杂 JSON 转为 String 存入数据库
    // 这里的 columnDefinition = "jsonb" 依赖 PostgreSQL 方言，
    // 如果嫌配置麻烦，可以直接用 "TEXT" 存储 JSON 字符串
    /** AI原始分析数据，存储JSON字符串 */
    @Column(name = "ai_analysis_raw", columnDefinition = "TEXT")
    private String aiAnalysisRaw;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    /** 获取记录ID */
    public Long getRecordId() {
        return recordId;
    }

    /** 设置记录ID */
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    /** 获取用户ID */
    public Long getUserId() {
        return userId;
    }

    /** 设置用户ID */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** 获取场景ID */
    public Long getScenarioId() {
        return scenarioId;
    }

    /** 设置场景ID */
    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    /** 获取音频文件URL */
    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    /** 设置音频文件URL */
    public void setAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    /** 获取音频时长 */
    public Integer getAudioDuration() {
        return audioDuration;
    }

    /** 设置音频时长 */
    public void setAudioDuration(Integer audioDuration) {
        this.audioDuration = audioDuration;
    }

    /** 获取总分 */
    public BigDecimal getScoreTotal() {
        return scoreTotal;
    }

    /** 设置总分 */
    public void setScoreTotal(BigDecimal scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

    /** 获取流畅度评分 */
    public BigDecimal getScoreFluency() {
        return scoreFluency;
    }

    /** 设置流畅度评分 */
    public void setScoreFluency(BigDecimal scoreFluency) {
        this.scoreFluency = scoreFluency;
    }

    /** 获取完整度评分 */
    public BigDecimal getScoreIntegrity() {
        return scoreIntegrity;
    }

    /** 设置完整度评分 */
    public void setScoreIntegrity(BigDecimal scoreIntegrity) {
        this.scoreIntegrity = scoreIntegrity;
    }

    /** 获取发音评分 */
    public BigDecimal getScorePronunciation() {
        return scorePronunciation;
    }

    /** 设置发音评分 */
    public void setScorePronunciation(BigDecimal scorePronunciation) {
        this.scorePronunciation = scorePronunciation;
    }

    /** 获取转录文本 */
    public String getTranscribedText() {
        return transcribedText;
    }

    /** 设置转录文本 */
    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }

    /** 获取AI原始分析数据 */
    public String getAiAnalysisRaw() {
        return aiAnalysisRaw;
    }

    /** 设置AI原始分析数据 */
    public void setAiAnalysisRaw(String aiAnalysisRaw) {
        this.aiAnalysisRaw = aiAnalysisRaw;
    }

    /** 获取创建时间 */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** 设置创建时间 */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}