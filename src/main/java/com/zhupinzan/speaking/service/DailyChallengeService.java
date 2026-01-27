package com.zhupinzan.speaking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import com.zhupinzan.speaking.repository.DailyTopicRepository;
import com.zhupinzan.speaking.service.business.TopicGeneratorTask;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * 每日挑战的服务层。
 * <p>
 * 该服务负责提供每日挑战的题目。它实现了一套包含缓存、AI生成和降级策略的健壮逻辑，
 * 以确保每日挑战功能的稳定性和高效性。
 */
@Service
@Slf4j
public class DailyChallengeService {

    private final DailyTopicRepository repo;     // 每日挑战题目的数据仓库
    private final TopicGeneratorTask generator;  // AI题目生成器
    private final ObjectMapper om = new ObjectMapper(); // 用于JSON序列化

    public DailyChallengeService(DailyTopicRepository repo, TopicGeneratorTask generator) {
        this.repo = repo;
        this.generator = generator;
    }

    /**
     * 获取或创建指定日期和用户画像的每日挑战题目。
     * <p>
     * 这是该服务的核心方法，其执行逻辑如下：
     * 1.  **缓存优先**: 首先尝试从数据库中查找是否已存在当天的题目。如果存在，则直接返回。
     * 2.  **AI生成**: 如果缓存中不存在，则调用 {@link TopicGeneratorTask} 尝试使用AI生成一个新的题目。
     * 3.  **降级处理**: 如果AI生成失败（例如，由于API错误或超时），则会创建一个静态的、预设的“兜底”题目。
     * 4.  **持久化**: 将新生成或降级创建的题目通过 upsert (更新或插入) 操作保存到数据库中，以供后续请求使用。
     *
     * @param date    题目的日期。
     * @param persona 用户的画像。
     * @return 一个 {@link DailyTopic} 实体，保证永不为null。
     */
    @Transactional
    public DailyTopic getOrCreate(LocalDate date, UserPersona persona) {
        String personaKey = persona.name();

        // 步骤 1: 优先从数据库缓存中获取
        var existing = repo.findByTopicDateAndPersona(date, personaKey);
        if (existing.isPresent()) {
            log.info("每日挑战缓存命中: date={}, persona={}", date, personaKey);
            return existing.get();
        }
        log.info("每日挑战缓存未命中，尝试生成新题目: date={}, persona={}", date, personaKey);

        // 步骤 2: 尝试使用AI生成
        DailyTopic generated;
        try {
            generated = generator.generateFor(date, persona);
        } catch (Exception e) {
            log.error("AI题目生成失败，将使用兜底题目: persona={}, error={}", personaKey, e.getMessage());
            generated = null;
        }

        // 步骤 3: 如果AI生成失败，创建静态的兜底题目
        if (generated == null) {
            generated = createFallback(date, persona);
        }

        // 步骤 4: 将新题目持久化到数据库
        try {
            // 使用仓库中的自定义upsert方法来插入或更新记录
            repo.upsertDailyTopic(
                date, personaKey, generated.getTitle(),
                generated.getPrompt(), generated.getImageUrl(),
                om.writeValueAsString(generated.getPayload())
            );
            // 再次查询以获取持久化后的完整实体
            return repo.findByTopicDateAndPersona(date, personaKey).orElse(generated);
        } catch (Exception e) {
            log.warn("每日挑战题目持久化失败，将返回一个临时的（未持久化的）题目对象。Error: {}", e.getMessage());
            // 即使持久化失败，也返回生成的题目对象，以保证前端功能的可用性。
            return generated;
        }
    }

    /**
     * 创建一个静态的、用于降级的兜底题目。
     *
     * @param date    题目日期。
     * @param persona 用户画像。
     * @return 一个包含预设内容的 {@link DailyTopic} 实体。
     */
    private DailyTopic createFallback(LocalDate date, UserPersona persona) {
        DailyTopic t = new DailyTopic();
        t.setTopicDate(date);
        t.setPersona(persona.name());
        t.setTargetPersona(persona);
        t.setTitle("今日挑战 (兜底)");
        t.setPrompt(persona == UserPersona.EXAM_PREP
            ? "Describe a book you have recently read."
            : "Talk about a challenge you faced at work.");
        t.setImageUrl("default_daily_bg");
        t.setPayload(Map.of("source", "static_fallback"));
        return t;
    }
}