package com.zhupinzan.speaking.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_progress")
@Data
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="device_id", nullable=false, unique=true, length=64)
    private String deviceId;

    @Column(name="total_attempts", nullable=false)
    private Integer totalAttempts;

    @Column(name="total_duration_ms", nullable=false)
    private Long totalDurationMs;

    @Column(name="last_active_date")
    private LocalDate lastActiveDate;

    @Column(name="streak_days", nullable=false)
    private Integer streakDays;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;
}