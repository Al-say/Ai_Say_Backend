package com.zhupinzan.speaking.service.business;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import com.zhupinzan.speaking.repository.DailyTopicRepository;
import com.zhupinzan.speaking.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * 题目生成定时任务
 * 每天凌晨3点预生成明天的题目
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class TopicGeneratorTask {

    private final DeepSeekService deepSeekService;
    private final DailyTopicRepository topicRepository;

    /**
     * 每天凌晨2点清理7天前的旧题目
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldTopics() {
        LocalDate cutoffDate = LocalDate.now(ZoneOffset.UTC).minusDays(7);

        log.info("开始清理{}之前的旧题目", cutoffDate);

        try {
            long deletedCount = topicRepository.deleteByTopicDateBefore(cutoffDate);
            log.info("✅ 成功清理{}条旧题目", deletedCount);
        } catch (Exception e) {
            log.error("清理旧题目失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每天凌晨3点执行，预生成明天的题目
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void generateTopicsForTomorrow() {
        LocalDate tomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1);

        log.info("开始预生成{}的题目", tomorrow);

        try {
            // 为备考党生成题目
            generateAndSave(UserPersona.EXAM_PREP, tomorrow);

            // 为职场人生成题目
            generateAndSave(UserPersona.CAREER_GROWTH, tomorrow);

            log.info("✅ {}的题目预生成完毕！", tomorrow);

        } catch (Exception e) {
            log.error("题目预生成失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 为指定用户画像生成题目（不保存，用于 Service 调用）
     */
    public DailyTopic generateFor(LocalDate date, UserPersona persona) {
        try {
            // 构建AI提示词
            String systemPrompt = buildSystemPrompt(persona);
            String userPrompt = buildUserPrompt(persona);

            // 调用DeepSeek生成题目
            String jsonResponse = deepSeekService.chat(systemPrompt, userPrompt);

            // 解析AI响应
            TopicGenerationResponse response = parseAiResponse(jsonResponse);

            // 创建 DailyTopic 对象
            DailyTopic topic = new DailyTopic();
            topic.setTitle(response.getTitle());
            topic.setDescription(response.getDescription());
            topic.setTargetPersona(persona);
            topic.setTopicDate(date);
            topic.setAiSuggestions(jsonResponse);
            topic.setPersona(persona.name());
            topic.setPrompt(response.getDescription()); // 使用 description 作为 prompt
            topic.setImageUrl("scene_daily_morning"); // 默认图片
            topic.setPayload(Map.of("source", "ai_generated"));

            log.info("成功生成{}的{}题目: {}", date, persona, response.getTitle());
            return topic;

        } catch (Exception e) {
            log.error("生成{}的{}题目失败: {}", date, persona, e.getMessage(), e);
            return null; // 返回 null，让 Service 用 fallback
        }
    }

    /**
     * 为指定用户画像生成并保存题目
     */
    private void generateAndSave(UserPersona persona, LocalDate date) {
        try {
            var topic = generateFor(date, persona);
            if (topic != null) {
                topicRepository.save(topic);
            } else {
                log.warn("生成失败，使用 fallback");
                var fallback = getFallbackTopic();
                DailyTopic topicFallback = new DailyTopic();
                topicFallback.setTitle(fallback.getTitle());
                topicFallback.setDescription(fallback.getDescription());
                topicFallback.setTargetPersona(persona);
                topicFallback.setTopicDate(date);
                topicFallback.setPersona(persona.name());
                topicFallback.setPrompt(fallback.getDescription());
                topicFallback.setImageUrl("scene_daily_morning");
                topicFallback.setPayload(Map.of("source", "fallback"));
                topicRepository.save(topicFallback);
            }

        } catch (Exception e) {
            log.error("保存{}的{}题目失败: {}", date, persona, e.getMessage(), e);
        }
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(UserPersona persona) {
        return switch (persona) {
            case EXAM_PREP -> """
                    You are a professional IELTS Speaking examiner.
                    Generate one speaking topic suitable for IELTS preparation.
                    Focus on academic vocabulary and formal expressions.
                    Return ONLY JSON format:
                    {
                        "title": "topic title",
                        "description": "detailed description with instructions"
                    }
                    """;

            case DAILY_LIFE -> """
                    You are a friendly English conversation coach.
                    Generate one speaking topic suitable for daily life communication.
                    Focus on everyday situations and casual expressions.
                    Return ONLY JSON format:
                    {
                        "title": "topic title",
                        "description": "detailed description with instructions"
                    }
                    """;

            case CAREER_GROWTH -> """
                    You are a business communication coach.
                    Generate one speaking topic suitable for workplace communication.
                    Focus on professional language and practical scenarios.
                    Return ONLY JSON format:
                    {
                        "title": "topic title",
                        "description": "detailed description with instructions"
                    }
                    """;
        };
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(UserPersona persona) {
        return switch (persona) {
            case EXAM_PREP -> "Generate an IELTS Speaking Part 2 style topic with clear instructions.";

            case DAILY_LIFE -> "Generate a casual daily life conversation topic with friendly instructions.";

            case CAREER_GROWTH -> "Generate a workplace communication scenario topic with practical instructions.";
        };
    }

    /**
     * 解析AI响应
     */
    private TopicGenerationResponse parseAiResponse(String jsonStr) {
        try {
            // 清理可能的markdown标记
            jsonStr = jsonStr.replace("```json", "").replace("```", "").trim();
            return JSON.parseObject(jsonStr, TopicGenerationResponse.class);
        } catch (Exception e) {
            log.warn("解析AI响应失败，使用默认题目: {}", e.getMessage());
            return getFallbackTopic();
        }
    }

    /**
     * 获取兜底题目
     */
    private TopicGenerationResponse getFallbackTopic() {
        TopicGenerationResponse fallback = new TopicGenerationResponse();
        fallback.setTitle("Describe Your Favorite Hobby");
        fallback.setDescription(
                "Talk about a hobby you enjoy. You should say: what the hobby is, how you started it, and why you like it. Describe it in detail for 1-2 minutes.");
        return fallback;
    }

    /**
     * AI响应解析类
     */
    private static class TopicGenerationResponse {
        private String title;
        private String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}