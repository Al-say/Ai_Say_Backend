package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.UserPersona;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 每日挑战题目实体
 * 用于缓存AI生成的题目，避免实时调用API
 */
@Entity
@Table(name = "daily_topics")
@Data
public class DailyTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 题目标题 */
    @Column(nullable = false)
    private String title;

    /** 题目详细描述 */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** 目标用户画像 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPersona targetPersona;

    /** 题目所属日期 */
    @Column(name = "for_date", nullable = false)
    private LocalDate topicDate;

    /** AI原始建议（可选，用于调试） */
    @Column(columnDefinition = "TEXT")
    private String aiSuggestions;

    /** 创建时间 */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // 新增字段
    @Column(length = 32)
    private String persona; // 存枚举名字符串

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(name = "image_url")
    private String imageUrl;

    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}