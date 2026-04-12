package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.UserProgress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.transaction.annotation.Transactional;

public class UserProgressRepositoryImpl implements UserProgressRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void upsertProgress(
            String deviceId,
            int attemptInc,
            long durationInc,
            LocalDate today,
            int newStreak
    ) {
        if (isPostgres()) {
            entityManager.createNativeQuery("""
                INSERT INTO user_progress
                    (device_id, total_attempts, total_duration_ms, last_active_date, streak_days, updated_at)
                VALUES (:deviceId, :attemptInc, :durationInc, :today, :newStreak, CURRENT_TIMESTAMP)
                ON CONFLICT (device_id)
                DO UPDATE SET
                    total_attempts = user_progress.total_attempts + EXCLUDED.total_attempts,
                    total_duration_ms = user_progress.total_duration_ms + EXCLUDED.total_duration_ms,
                    last_active_date = EXCLUDED.last_active_date,
                    streak_days = EXCLUDED.streak_days,
                    updated_at = CURRENT_TIMESTAMP
                """)
                .setParameter("deviceId", deviceId)
                .setParameter("attemptInc", attemptInc)
                .setParameter("durationInc", durationInc)
                .setParameter("today", today)
                .setParameter("newStreak", newStreak)
                .executeUpdate();
            return;
        }

        UserProgress progress = findByDeviceId(deviceId);
        if (progress == null) {
            progress = new UserProgress();
            progress.setDeviceId(deviceId);
            progress.setTotalAttempts(0);
            progress.setTotalDurationMs(0L);
            progress.setStreakDays(0);
        }

        progress.setTotalAttempts(progress.getTotalAttempts() + attemptInc);
        progress.setTotalDurationMs(progress.getTotalDurationMs() + durationInc);
        progress.setLastActiveDate(today);
        progress.setStreakDays(newStreak);
        progress.setUpdatedAt(OffsetDateTime.now());

        if (progress.getId() == null) {
            entityManager.persist(progress);
        } else {
            entityManager.merge(progress);
        }
    }

    private UserProgress findByDeviceId(String deviceId) {
        try {
            return entityManager.createQuery(
                    "select u from UserProgress u where u.deviceId = :deviceId", UserProgress.class)
                .setParameter("deviceId", deviceId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private boolean isPostgres() {
        return JpaUtils.isPostgres(entityManager);
    }
}
