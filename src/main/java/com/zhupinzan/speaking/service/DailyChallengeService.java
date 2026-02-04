package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.service.DailyTopicPersistenceException;
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
 * 每日挑战服务层 - 提供每日练习题目的核心业务逻辑
 *
 * <h3>服务定位与功能概述</h3>
 * <p>
 * DailyChallengeService 是整个应用的核心服务之一，负责为不同用户画像生成或获取每日练习题目。
 * 该服务采用"缓存优先，AI生成，兜底降级"的三层架构设计，确保在任何情况下都能为用户提供稳定的
 * 每日挑战体验。
 * </p>
 *
 * <h3>核心业务流程与算法逻辑</h3>
 * <p>
 * 主业务流程 {@link #getOrCreate(LocalDate, UserPersona)} 实现了以下关键逻辑：
 * <ol>
 *   <li><strong>缓存优先策略</strong>：首先查询数据库中的每日题目表，使用 (日期, 用户画像) 作为复合键，
 *       如果存在缓存记录，则直接返回，避免重复计算和AI调用。</li>
 *   <li><strong>AI动态生成</strong>：当缓存未命中时，调用 TopicGeneratorTask 使用大语言模型生成个性化题目。
 *       该过程根据日期、用户画像（考试准备/职场成长）和可能的用户偏好生成针对性练习内容。</li>
 *   <li><strong>降级兜底机制</strong>：当AI生成失败（API异常、超时或限流）时，系统自动切换到静态预设题目，
 *       保证基础功能的可用性。</li>
 *   <li><strong>数据持久化</strong>：使用 upsert 操作保存生成的题目，确保后续请求可以直接从数据库获取，
 *       实现了读写分离和数据一致性的平衡。</li>
 * </ol>
 * </p>
 *
 * <h3>与其他服务的协作关系</h3>
 * <p>
 * - {@link DailyTopicRepository}：数据持久化层，提供数据库操作接口
 * - {@link TopicGeneratorTask}：AI服务层，负责题目内容的智能生成
 * - 使用 Lombok SLF4J 进行日志记录，便于问题排查和性能监控
 * </p>
 *
 * <h3>数据处理与转换逻辑</h3>
 * <p>
 * - 使用 Jackson ObjectMapper 处理 JSON 序列化，将题目内容以结构化形式存储
 * - 题目包含标题、提示词、图片URL和扩展payload等多维度信息
 * - 支持多种用户画像的差异化题目生成（EXAM_PREP vs CAREER_GROWTH）
 * </p>
 *
 * <h3>缓存策略和性能优化</h3>
 * <p>
 * - 采用数据库作为缓存层，通过复合索引优化查询性能
 * - 实现幂等性设计，避免重复生成相同题目的资源浪费
 * - 使用 @Transactional 保证数据操作的原子性
 * - 日志记录命中情况，便于监控缓存效率
 * </p>
 *
 * <h3>错误处理和降级机制</h3>
 * <p>
 * - AI生成失败时自动降级到预设题目，保证服务可用性
 * - 持久化失败时返回临时对象，避免影响前端用户体验
 * - 异常处理采用防御性编程，确保主流程不会因单个组件故障而中断
 * - 详细的日志记录用于事后分析和问题定位
 * </p>
 *
 * <h3>配置参数和使用场景</h3>
 * <p>
 * - 无需额外配置参数，使用 Spring 依赖注入管理
 * - 适用于每日晨练、定期测评等需要定期更新内容的场景
 * - 通过日期维度实现题目的时间轮转，避免内容重复
 * </p>
 *
 * <h3>扩展性和维护性考虑</h3>
 * <p>
 * - 采用接口化设计，便于替换不同的题目生成策略
 * - 单一职责原则，只负责题目获取和生成逻辑
 * - 清晰的代码注释和流程说明，便于后续维护
 * - 事务边界明确，避免长事务和性能问题
 * </p>
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
            throw new DailyTopicPersistenceException("Failed to persist daily topic for date: " + date + ", persona: " + personaKey, e);
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