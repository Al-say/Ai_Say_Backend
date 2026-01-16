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

@Service
@Slf4j
public class DailyChallengeService {

    private final DailyTopicRepository repo;
    private final TopicGeneratorTask generator;
    private final ObjectMapper om = new ObjectMapper();

    public DailyChallengeService(DailyTopicRepository repo, TopicGeneratorTask generator) {
        this.repo = repo;
        this.generator = generator;
    }

    @Transactional
    public DailyTopic getOrCreate(LocalDate date, UserPersona persona) {
        String personaKey = persona.name();

        // 1. 缓存优先
        var existing = repo.findByTopicDateAndPersona(date, personaKey);
        if (existing.isPresent()) return existing.get();

        // 2. 尝试 AI 生成 (带异常拦截)
        DailyTopic generated;
        try {
            generated = generator.generateFor(date, persona);
        } catch (Exception e) {
            log.error("AI Topic Generation Failed for {}: {}", personaKey, e.getMessage());
            generated = null;
        }

        // 3. 静态兜底
        if (generated == null) {
            generated = createFallback(date, persona);
        }

        try {
            repo.upsertDailyTopic(
                date, personaKey, generated.getTitle(),
                generated.getPrompt(), generated.getImageUrl(),
                om.writeValueAsString(generated.getPayload())
            );
            return repo.findByTopicDateAndPersona(date, personaKey).orElse(generated);
        } catch (Exception e) {
            log.warn("Failed to persist daily topic, returning volatile object.");
            return generated;
        }
    }

    private DailyTopic createFallback(LocalDate date, UserPersona persona) {
        DailyTopic t = new DailyTopic();
        t.setTopicDate(date);
        t.setPersona(persona.name());
        t.setTitle("今日挑战");
        t.setPrompt(persona == UserPersona.EXAM_PREP 
            ? "Describe a hobby you would like to start." 
            : "Talk about how you handle a busy workday.");
        t.setImageUrl("default_daily_bg");
        t.setPayload(Map.of("source", "static_fallback"));
        return t;
    }
}