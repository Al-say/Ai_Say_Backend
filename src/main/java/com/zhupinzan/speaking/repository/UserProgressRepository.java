package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByDeviceId(String deviceId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO user_progress(device_id, total_attempts, total_duration_ms, last_active_date, streak_days, updated_at)
        VALUES (:deviceId, :attemptInc, :durationInc, :today, :newStreak, CURRENT_TIMESTAMP)
        ON CONFLICT (device_id)
        DO UPDATE SET
          total_attempts = user_progress.total_attempts + :attemptInc,
          total_duration_ms = user_progress.total_duration_ms + :durationInc,
          last_active_date = :today,
          streak_days = :newStreak,
          updated_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void upsertProgress(
            @Param("deviceId") String deviceId,
            @Param("attemptInc") int attemptInc,
            @Param("durationInc") long durationInc,
            @Param("today") LocalDate today,
            @Param("newStreak") int newStreak
    );
}