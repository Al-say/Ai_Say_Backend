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
 * 每日挑战服务层 - 提供每日练习题目的核心业务逻辑。
 *
 * <h3>服务定位与功能概述</h3>
 * <p>
 * DailyChallengeService 是整个应用的核心服务之一，负责为不同用户画像（如备考党、职场人）动态生成或获取每日练习题目。
 * 本服务旨在提供一个稳定、可靠且内容丰富的每日挑战功能，鼓励用户持续练习。
 * </p>
 * <p>
 * 服务采用“缓存优先，AI生成，兜底降级”的三层架构设计，确保在任何情况下都能为用户提供高质量的
 * 每日挑战体验。这种设计兼顾了性能、成本和用户体验。
 * </p>
 *
 * <h3>核心业务流程与算法逻辑</h3>
 * <p>
 * 主业务流程 {@link #getOrCreate(LocalDate, UserPersona)} 实现了以下关键逻辑：
 * <ol>
 *   <li><strong>缓存优先策略 (Cache-First)</strong>：
 *       首先查询数据库中是否已存在当天的题目。使用 (日期, 用户画像) 作为复合键进行查找。
 *       如果存在缓存记录，则直接返回，避免了不必要的计算和AI调用，提高了响应速度并降低了成本。
 *   </li>
 *   <li><strong>AI动态生成 (AI Generation)</strong>：
 *       当缓存未命中时，表明当天的题目尚未生成。此时会调用 {@link TopicGeneratorTask} 服务，
 *       利用大语言模型（如DeepSeek）根据日期和用户画像动态生成一个全新的、个性化的题目。
 *   </li>
 *   <li><strong>降级兜底机制 (Fallback)</strong>：
 *       考虑到AI服务可能因网络问题、API限流或内部错误而失败，设计了可靠的降级机制。
 *       当AI生成失败时，系统会自动切换到一个静态的、预设的“兜底”题目。这保证了即时在最坏情况下，
 *       用户依然能获得一个可用的练习题目，保证了核心功能的可用性。
 *   </li>
 *   <li><strong>数据持久化 (Persistence)</strong>：
 *       无论是AI生成的题目还是兜底题目，一旦创建，都会通过“UPSERT”（更新或插入）操作保存到数据库中。
 *       这样，后续对同一天同一画像的请求就可以直接从缓存中获取，实现了“一次计算，多次使用”的模式。
 *   </li>
 * </ol>
 * </p>
 *
 * <h3>与其他服务的协作关系</h3>
 * <ul>
 *   <li><b>{@link DailyTopicRepository}</b>：数据持久化层。本服务通过该接口与数据库交互，进行题目的读写操作。</li>
 *   <li><b>{@link TopicGeneratorTask}</b>：AI服务层。本服务委托该任务进行AI题目的智能生成。</li>
 *   <li><b>Lombok SLF4J</b>：用于日志记录，方便追踪程序执行流程、排查问题和进行性能监控。</li>
 * </ul>
 *
 * <h3>错误处理和降级机制</h3>
 * <ul>
 *   <li><b>AI生成失败</b>：通过 try-catch 块捕获所有来自 {@code TopicGeneratorTask} 的异常，记录错误日志，并触发 {@code createFallback} 逻辑，无缝切换到兜底题目。</li>
 *   <li><b>持久化失败</b>：同样通过 try-catch 块捕获数据库操作异常。为了最大限度保证用户体验，即使持久化失败，也会将生成的临时题目对象返回给前端，同时抛出 {@link DailyTopicPersistenceException} 以便上层统一处理和监控。这种策略保证了前端在数据库短暂故障时仍能工作。</li>
 * </ul>
 *
 * <h3>潜在 Bug 及常见问题 (Potential Bugs and Common Issues)</h3>
 * <p>在 {@code DailyChallengeService} 的运行中，以下是一些可能出现的 bug 或常见问题，需要开发和运维人员特别注意：</p>
 * <ol>
 *   <li><b>数据库唯一约束冲突 (Unique Constraint Violation)</b>：
 *       <ul>
 *         <li><b>现象</b>：当尝试使用 UPSERT 机制（`INSERT ... ON CONFLICT ...`）保存 DailyTopic 时，如果数据库表中缺少针对 `(for_date, persona)` 的唯一约束（UNIQUE INDEX 或 PRIMARY KEY），则 UPSERT 操作会失败，导致 `ERROR: there is no unique or exclusion constraint matching the ON CONFLICT specification` 错误。</li>
 *         <li><b>原因</b>：DDL 自动生成工具（如 Hibernate `ddl-auto`）可能不会为非主键字段自动创建唯一的复合索引。</li>
 *         <li><b>解决方案</b>：手动在数据库中为 `daily_topics` 表的 `(for_date, persona)` 列添加唯一约束。</li>
 *       </ul>
 *   </li>
 *   <li><b>AI服务调用失败 (AI Service Call Failure)</b>：
 *       <ul>
 *         <li><b>现象</b>：AI 题目生成（`TopicGeneratorTask.generateFor`）抛出异常，日志中出现 `AI题目生成失败`。这会导致服务降级到兜底题目。</li>
 *         <li><b>原因</b>：网络问题、DeepSeek API 不可用、API Key 过期或错误、请求参数不符合 AI 接口规范、AI 服务返回了不符合预期的响应格式。</li>
 *         <li><b>解决方案</b>：检查网络连接、验证 `deepseek.api.key` 和 `deepseek.api.url` 配置、检查 AI 服务状态、分析 AI 服务的日志和响应体。</li>
 *       </ul>
 *   </li>
 *   <li><b>JSON 序列化/反序列化问题 (JSON Serialization/Deserialization)</b>：
 *       <ul>
 *         <li><b>现象</b>：在将 `DailyTopic` 的 `payload` 字段保存为 JSONB 类型时，或从 AI 服务解析 JSON 响应时，可能发生 `JsonProcessingException` 或其他 JSON 格式错误。</li>
 *         <li><b>原因</b>：`ObjectMapper` 序列化对象失败，或 AI 服务返回的 JSON 字符串结构与预期的 `TopicGenerationResponse` 不匹配。</li>
 *         <li><b>解决方案</b>：确保 `payload` 对象总是可以被正确序列化，并对 AI 服务的响应进行严格的结构验证和健壮的错误处理。</li>
 *       </ul>
 *   </li>
 *   <li><b>并发竞争问题 (Concurrency Issues)</b>：
 *       <ul>
 *         <li><b>现象</b>：在极高并发场景下，多个请求同时尝试为同一天同一用户画像生成题目，可能导致性能下降或不必要的工作重复。</li>
 *         <li><b>原因</b>：尽管 `getOrCreate` 采用了缓存和 UPSERT 机制，但在缓存失效的瞬间，仍可能出现短暂的竞争窗口。</li>
 *         <li><b>解决方案</b>：利用分布式锁（如基于 Redis 的锁）进一步优化热点数据的并发访问。</li>
 *       </ul>
 *   </li>
 *   <li><b>配置错误 (Configuration Errors)</b>：
 *       <ul>
 *         <li><b>现象</b>：服务启动失败或 AI 调用、数据库连接异常。</li>
 *         <li><b>原因</b>：`application.properties` 或环境变量中 `deepseek.api.key`、`deepseek.api.url`、数据库连接信息等配置错误。</li>
 *         <li><b>解决方案</b>：仔细检查所有相关的配置项，确保其正确性和有效性。</li>
 *       </ul>
 *   </li>
 * </ol>
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
     * 这是该服务的核心方法，它 orchestrates 了整个题目的获取、生成和持久化流程。
     * 其执行逻辑严格遵循“缓存优先、AI生成、兜底降级、最终持久化”的策略。
     * <p>
     * <b>详细执行步骤:</b>
     * <ol>
     *   <li>
     *     <b>步骤 1: 缓存优先 (Cache-First)</b><br>
     *     首先，使用日期和用户画像作为联合键，尝试从数据库 ({@code DailyTopicRepository}) 中查找是否已存在当天的题目。
     *     如果找到，记录一条缓存命中的日志，并立即返回该题目，终止后续流程。
     *   </li>
     *   <li>
     *     <b>步骤 2: AI生成 (AI Generation)</b><br>
     *     如果缓存中不存在，记录缓存未命中的日志，并调用 {@link TopicGeneratorTask#generateFor(LocalDate, UserPersona)} 尝试使用AI生成一个新的题目。
     *     此过程被一个 try-catch 块包裹，以处理任何可能的AI服务异常。
     *   </li>
     *   <li>
     *     <b>步骤 3: 降级处理 (Fallback)</b><br>
     *     如果AI生成失败（即 {@code generator.generateFor} 抛出异常），catch块会记录错误日志，并将生成的题目变量设为 {@code null}。
     *     后续的 {@code if (generated == null)} 判断会捕捉到这个情况，并调用 {@link #createFallback(LocalDate, UserPersona)} 方法创建一个静态的、预设的“兜底”题目。
     *     这确保了即使AI服务完全不可用，用户也能得到一个有效的题目。
     *   </li>
     *   <li>
     *     <b>步骤 4: 持久化 (Persistence)</b><br>
     *     无论是AI生成的题目还是兜底题目，都会被传递到此步骤进行持久化。
     *     使用 {@link DailyTopicRepository#upsertDailyTopic} 方法将题目数据“更新或插入”(UPSERT) 到数据库中。
     *     这个操作也被 try-catch 块保护。如果持久化失败，会记录警告日志并抛出 {@link DailyTopicPersistenceException}，
     *     以通知上层调用者持久化环节出现问题，便于监控和报警。
     *   </li>
     * </ol>
     *
     * @param date    题目的日期，通常是当天的UTC日期。
     * @param persona 用户的画像 (e.g., EXAM_PREP, CAREER_GROWTH)。
     * @return 一个 {@link DailyTopic} 实体。在绝大多数情况下，这是一个已持久化的实体。如果持久化失败，会返回一个临时的（未持久化的）实体。
     * @throws DailyTopicPersistenceException 如果将生成的题目持久化到数据库失败。
     */
    @Transactional
    public DailyTopic getOrCreate(LocalDate date, UserPersona persona) {
        String personaKey = persona.name();

        // 步骤 1: 优先从数据库缓存中获取。这是核心的性能优化点。
        var existing = repo.findByTopicDateAndPersona(date, personaKey);
        if (existing.isPresent()) {
            log.info("每日挑战缓存命中: date={}, persona={}", date, personaKey);
            return existing.get();
        }
        log.info("每日挑战缓存未命中，尝试生成新题目: date={}, persona={}", date, personaKey);

        // 步骤 2: 缓存未命中，尝试调用AI服务生成新题目。
        DailyTopic generated;
        try {
            generated = generator.generateFor(date, persona);
        } catch (Exception e) {
            // 如果AI生成失败，记录错误并准备使用兜底方案。
            log.error("AI题目生成失败，将使用兜底题目: persona={}, error={}", personaKey, e.getMessage());
            generated = null;
        }

        // 步骤 3: 检查AI生成是否成功。如果不成功 (generated为null)，则创建静态的兜底题目。
        if (generated == null) {
            log.info("AI生成失败或返回null，正在创建兜底题目: persona={}", personaKey);
            generated = createFallback(date, persona);
        }

        // 步骤 4: 将新生成（或兜底）的题目持久化到数据库，以供后续请求使用。
        try {
            // 使用仓库中的自定义upsert方法来插入或更新记录。
            repo.upsertDailyTopic(
                date, personaKey, generated.getTitle(),
                generated.getPrompt(), generated.getImageUrl(),
                om.writeValueAsString(generated.getPayload())
            );
            // 再次查询以获取持久化后的完整实体，确保返回的是带有ID和其他数据库生成字段的最新状态。
            return repo.findByTopicDateAndPersona(date, personaKey).orElse(generated);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 如果是重复键异常，说明记录已存在，尝试更新
            log.info("记录已存在，正在更新: date={}, persona={}", date, personaKey);
            try {
                repo.updateDailyTopic(
                    date, personaKey, generated.getTitle(),
                    generated.getPrompt(), generated.getImageUrl(),
                    om.writeValueAsString(generated.getPayload())
                );
                return repo.findByTopicDateAndPersona(date, personaKey).orElse(generated);
            } catch (Exception updateException) {
                log.warn("更新每日挑战题目失败，将返回一个临时的（未持久化的）题目对象。Error: {}", updateException.getMessage());
                throw new DailyTopicPersistenceException("Failed to update daily topic for date: " + date + ", persona: " + personaKey, updateException);
            }
        } catch (Exception e) {
            // 如果持久化失败，记录警告并抛出自定义异常，以便进行监控。
            log.warn("每日挑战题目持久化失败，将返回一个临时的（未持久化的）题目对象。Error: {}", e.getMessage());
            // 抛出异常，让全局异常处理器或调用方知道持久化失败了。
            throw new DailyTopicPersistenceException("Failed to persist daily topic for date: " + date + ", persona: " + personaKey, e);
        }
    }

    /**
     * 创建一个静态的、用于降级的兜底题目。
     * <p>
     * 当AI服务不可用或生成失败时，此方法提供一个预设的、通用的题目，
     * 以保证每日挑战功能的基本可用性。
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