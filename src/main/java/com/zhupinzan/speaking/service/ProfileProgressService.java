package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * 用户进度服务 - 负责更新用户练习进度和连续打卡
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileProgressService {

    private final UserProgressRepository userProgressRepository;

    /**
     * 当用户完成一次练习时调用
     * 
     * @param deviceId   设备ID
     * @param durationMs 练习时长(毫秒)
     */
    @Transactional
    public void onPracticeCompleted(String deviceId, long durationMs) {
        log.info("📊 更新用户进度 - 设备: {}, 时长: {}ms", deviceId, durationMs);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        var current = userProgressRepository.findByDeviceId(deviceId).orElse(null);

        int newStreak;
        if (current == null || current.getLastActiveDate() == null) {
            newStreak = 1;
        } else {
            LocalDate last = current.getLastActiveDate();
            if (last.isEqual(today)) {
                newStreak = current.getStreakDays();
            } else if (last.plusDays(1).isEqual(today)) {
                newStreak = current.getStreakDays() + 1;
            } else {
                newStreak = 1;
            }
        }

        userProgressRepository.upsertProgress(deviceId, 1, durationMs, today, newStreak);
        log.info("✅ 用户进度更新请求已发送 (upsert) - 设备: {}, newStreak: {}", deviceId, newStreak);
    }
}