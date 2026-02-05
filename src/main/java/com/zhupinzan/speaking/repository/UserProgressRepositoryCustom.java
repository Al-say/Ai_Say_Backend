package com.zhupinzan.speaking.repository;

import java.time.LocalDate;

public interface UserProgressRepositoryCustom {
    void upsertProgress(
            String deviceId,
            int attemptInc,
            long durationInc,
            LocalDate today,
            int newStreak
    );
}
