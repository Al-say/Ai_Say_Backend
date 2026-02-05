package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 异步评估任务实体
 * 用于将 AsyncEvaluationService 管理的评估任务持久化到数据库
 */
@Entity
@Table(name = "evaluation_tasks")
@Data
public class EvaluationTask {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 36)
    private String id; // 使用 UUID 字符串

    @Column(nullable = false)
    private String userEmail; // 绑定用户

    @Column(columnDefinition = "TEXT") // 允许存储长文本
    private String originalText;

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, COMPLETED, FAILED

    @Column(columnDefinition = "TEXT") // 存储 DeepSeek 返回的大段 JSON
    private String resultJson;

    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
