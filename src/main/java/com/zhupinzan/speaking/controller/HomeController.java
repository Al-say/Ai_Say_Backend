package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import com.zhupinzan.speaking.repository.DailyTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final DailyTopicRepository topicRepository;

    @GetMapping
    public ResponseEntity<?> getHome() {
        return ResponseEntity.ok("主页模块 - 欢迎使用AI说话应用");
    }

    /**
     * 获取每日挑战题目
     * @param persona 用户画像 (EXAM_PREP 或 CAREER_GROWTH)
     * @return 今日挑战题目
     */
    @GetMapping("/daily-challenge")
    public ResponseEntity<?> getDailyChallenge(@RequestParam UserPersona persona) {
        LocalDate today = LocalDate.now();

        Optional<DailyTopic> topicOpt = topicRepository.findByTargetPersonaAndForDate(persona, today);

        if (topicOpt.isPresent()) {
            DailyTopic topic = topicOpt.get();
            return ResponseEntity.ok(new DailyChallengeResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getTargetPersona(),
                topic.getForDate()
            ));
        } else {
            // 如果没有预生成的题目，返回兜底题目
            return ResponseEntity.ok(new DailyChallengeResponse(
                null,
                "Describe Your Daily Routine",
                "Talk about your typical day. You should say: what time you wake up, what activities you do, and how you feel about your routine. Describe it in detail for 1-2 minutes.",
                persona,
                today
            ));
        }
    }

    /**
     * 每日挑战响应DTO
     */
    public static class DailyChallengeResponse {
        private final Long id;
        private final String title;
        private final String description;
        private final UserPersona targetPersona;
        private final LocalDate forDate;

        public DailyChallengeResponse(Long id, String title, String description,
                                    UserPersona targetPersona, LocalDate forDate) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.targetPersona = targetPersona;
            this.forDate = forDate;
        }

        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public UserPersona getTargetPersona() { return targetPersona; }
        public LocalDate getForDate() { return forDate; }
    }
}