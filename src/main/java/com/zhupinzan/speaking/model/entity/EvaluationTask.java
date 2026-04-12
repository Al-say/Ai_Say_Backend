package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.TaskStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "evaluation_tasks")
@Data
public class EvaluationTask {

    @Id
    private String id; // 使用 UUID 字符串

    @Column(name = "user_identity", nullable = false)
    private String userIdentity; // 绑定用户

    @Column(nullable = false)
    private String persona;

    @Column(nullable = false)
    private String scene;

    @Column(name = "transcript", columnDefinition = "TEXT")
    private String transcript;

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // PENDING, COMPLETED, FAILED

    private Integer progress;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "jsonb")
    private String result;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}