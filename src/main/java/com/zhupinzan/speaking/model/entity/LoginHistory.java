package com.zhupinzan.speaking.model.entity;

import com.zhupinzan.speaking.model.LoginType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 登录历史记录实体
 */
@Entity
@Table(name = "login_history")
@Data
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false, length = 16)
    private LoginType loginType;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "login_at", nullable = false)
    private OffsetDateTime loginAt;

    @PrePersist
    protected void onCreate() {
        if (loginAt == null) {
            loginAt = OffsetDateTime.now();
        }
    }
}
