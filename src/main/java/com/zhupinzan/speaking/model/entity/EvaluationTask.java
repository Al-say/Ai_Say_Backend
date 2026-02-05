package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_tasks")
@Data
public class EvaluationTask {

    @Id
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