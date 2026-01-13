package com.zhupinzan.speaking.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_records")
public class AssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "user_id")
    private Long userId; // 暂时直接存ID，后续可关联 User 实体

    @Column(name = "scenario_id")
    private Long scenarioId; // 对应场景 ID

    // 音频相关 (如果是纯文本评测，这里可为空)
    @Column(name = "audio_file_url")
    private String audioFileUrl;

    @Column(name = "audio_duration")
    private Integer audioDuration;

    // --- 核心评分字段 ---
    @Column(name = "score_total")
    private BigDecimal scoreTotal;

    @Column(name = "score_fluency")
    private BigDecimal scoreFluency;

    @Column(name = "score_integrity")
    private BigDecimal scoreIntegrity; // 完整度/逻辑分

    @Column(name = "score_pronunciation")
    private BigDecimal scorePronunciation; // 文本评测时该项可能为0或不适用

    // --- 文本内容 ---
    @Column(name = "transcribed_text", columnDefinition = "TEXT")
    private String transcribedText;

    // --- AI 原始分析数据 ---
    // 为了简化开发，我们将 AI 返回的复杂 JSON 转为 String 存入数据库
    // 这里的 columnDefinition = "jsonb" 依赖 PostgreSQL 方言，
    // 如果嫌配置麻烦，可以直接用 "TEXT" 存储 JSON 字符串
    @Column(name = "ai_analysis_raw", columnDefinition = "TEXT")
    private String aiAnalysisRaw;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(Long scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    public void setAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    public Integer getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(Integer audioDuration) {
        this.audioDuration = audioDuration;
    }

    public BigDecimal getScoreTotal() {
        return scoreTotal;
    }

    public void setScoreTotal(BigDecimal scoreTotal) {
        this.scoreTotal = scoreTotal;
    }

    public BigDecimal getScoreFluency() {
        return scoreFluency;
    }

    public void setScoreFluency(BigDecimal scoreFluency) {
        this.scoreFluency = scoreFluency;
    }

    public BigDecimal getScoreIntegrity() {
        return scoreIntegrity;
    }

    public void setScoreIntegrity(BigDecimal scoreIntegrity) {
        this.scoreIntegrity = scoreIntegrity;
    }

    public BigDecimal getScorePronunciation() {
        return scorePronunciation;
    }

    public void setScorePronunciation(BigDecimal scorePronunciation) {
        this.scorePronunciation = scorePronunciation;
    }

    public String getTranscribedText() {
        return transcribedText;
    }

    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }

    public String getAiAnalysisRaw() {
        return aiAnalysisRaw;
    }

    public void setAiAnalysisRaw(String aiAnalysisRaw) {
        this.aiAnalysisRaw = aiAnalysisRaw;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}