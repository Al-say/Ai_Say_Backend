package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.UserPersona;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "assessment_record")
@Data
public class AssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // ✅ 补齐
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AssessmentMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserPersona persona;

    @Column(nullable = false, length = 32) // ✅ 补齐
    private String scene = "practice";

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "overall_score")
    private Double overallScore;

    // 扁平列
    private Double fluency;
    private Double completeness;
    private Double relevance;

    // JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> metrics = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> feedback = new HashMap<>();

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    // ✅ 生命周期钩子：统一维护时间与数据一致性
    @PrePersist
    protected void onCreate() {
        var now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        syncCoreMetrics();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now(); // ✅ 应用层自动更新
        syncCoreMetrics();
    }

    private void syncCoreMetrics() {
        if (metrics == null) metrics = new HashMap<>();
        if (fluency != null) metrics.put("fluency", fluency);
        if (completeness != null) metrics.put("completeness", completeness);
        if (relevance != null) metrics.put("relevance", relevance);
    }
}