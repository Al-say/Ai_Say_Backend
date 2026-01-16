package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.repository.UserProgressRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserProgressRepository userProgressRepo;

    public ProfileController(UserProgressRepository userProgressRepo) {
        this.userProgressRepo = userProgressRepo;
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok("我的模块 - 个人中心和设置");
    }

    public record ProfileStatsDTO(
        String deviceId,
        int streakDays,
        String lastActiveDate,
        int totalAttempts,
        long totalDurationMs
    ) {}

    @GetMapping("/stats")
    public ResponseEntity<ProfileStatsDTO> stats(@RequestParam String deviceId) {
        var p = userProgressRepo.findByDeviceId(deviceId).orElse(null);
        if (p == null) {
            return ResponseEntity.ok(new ProfileStatsDTO(deviceId, 0, null, 0, 0L));
        }
        return ResponseEntity.ok(new ProfileStatsDTO(
            deviceId,
            p.getStreakDays(),
            p.getLastActiveDate() == null ? null : p.getLastActiveDate().toString(),
            p.getTotalAttempts(),
            p.getTotalDurationMs()
        ));
    }
}