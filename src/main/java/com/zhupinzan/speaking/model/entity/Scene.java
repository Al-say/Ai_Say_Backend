package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.UserPersona;
import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "scene")
@Data
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_persona", length = 32) // ✅ 显式指定列名
    private UserPersona targetPersona;

    @Column(name = "initial_prompt", columnDefinition = "TEXT") // ✅ 显式指定列名
    private String initialPrompt;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }
}