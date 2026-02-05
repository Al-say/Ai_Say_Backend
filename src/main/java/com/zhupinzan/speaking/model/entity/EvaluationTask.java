package com.zhupinzan.speaking.model.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 异步评估任务实体
 * 用于将 AsyncEvaluationService 管理的评估任务持久化到数据库
 */
@Entity
@Table(name = "evaluation_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationTask {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 36)
    private String id; // Use taskId as the primary key

    @Column(name = "user_identity", nullable = false)
    private String userIdentity;

    @Enumerated(EnumType.STRING)
    @Column(name = "persona", nullable = false)
    private UserPersona persona;

    @Column(name = "scene", nullable = false)
    private String scene;

    @Column(name = "transcript", columnDefinition = "TEXT", nullable = false)
    private String transcript;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AsyncEvaluationResponse.TaskStatus status;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(JsonBinaryType.class)
    @Column(name = "result", columnDefinition = "jsonb")
    private DeepSeekEvalResult result;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Optional: Add other fields from AsyncEvaluationResponse if needed,
    // e.g., estimatedSecondsRemaining, but for persistence, the core fields are sufficient.
}
