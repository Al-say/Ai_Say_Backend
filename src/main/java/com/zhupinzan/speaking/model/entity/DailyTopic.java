package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.UserPersona;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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
    @Column(nullable = false)
    private LocalDate forDate;

    /** AI原始建议（可选，用于调试） */
    @Column(columnDefinition = "TEXT")
    private String aiSuggestions;

    /** 创建时间 */
    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();
}