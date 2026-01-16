package com.zhupinzan.speaking.service.business;

import com.zhupinzan.speaking.repository.UserProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class ProfileProgressService {

    private final UserProgressRepository repo;

    public ProfileProgressService(UserProgressRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void onPracticeCompleted(String deviceId, long durationMs) {
        var today = LocalDate.now(ZoneOffset.UTC);

        var current = repo.findByDeviceId(deviceId).orElse(null);
        int newStreak;
        if (current == null || current.getLastActiveDate() == null) {
            newStreak = 1;
        } else {
            var last = current.getLastActiveDate();
            if (last.isEqual(today)) newStreak = current.getStreakDays();
            else if (last.plusDays(1).isEqual(today)) newStreak = current.getStreakDays() + 1;
            else newStreak = 1;
        }

        repo.upsertProgress(deviceId, 1, durationMs, today, newStreak);
    }
}