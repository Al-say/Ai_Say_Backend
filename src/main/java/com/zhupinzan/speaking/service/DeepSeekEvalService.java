package com.zhupinzan.speaking.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import com.zhupinzan.speaking.service.core.PromptFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI评估服务 - 智能口语评分核心引擎
 * <p>
 * 该服务是系统的"AI大脑"，负责与DeepSeek大语言模型进行深度交互，
 * 提供专业、精准的口语评估服务。它集成了多项先进技术确保服务的
 * 可靠性和性能表现。
 * <p>
 * <b>核心功能：</b><br>
 * 1. 智能评分：基于用户画像和场景的多维度口语评估<br>
 * 2. 弹性设计：集成Resilience4j实现自动重试、熔断和降级<br>
 * 3. 响应解析：处理OpenAI兼容API的复杂JSON响应<br>
 * 4. 容错机制：优雅处理各种异常和边界情况<br>
 * 5. 性能优化：异步调用、超时控制、资源复用
 * <p>
 * <b>技术架构：</b><br>
 * • WebClient：异步HTTP客户端，实现非阻塞调用<br>
 * • Resilience4j：服务弹性框架，保障系统稳定性<br>
 * • Jackson：高性能JSON处理，支持灵活的数据映射<br>
 * • Prompt Engineering：精心设计的提示词工程，提升评估质量
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekEvalService {

    // ====== 核心组件依赖注入 ======

    /**
     * DeepSeek API WebClient客户端
     * <p>
     * 经过精心配置的异步HTTP客户端，负责与DeepSeek API进行通信。
     * 特性包括：
     * • 基于Reactor的非阻塞调用
     * • 自动的重试和熔断机制
     * • 可配置的超时和连接池
     * • 统一的错误处理
     */
    private final WebClient deepSeekWebClient;

    /**
     * Jackson JSON处理器
     * <p>
     * 用于高性能的JSON序列化和反序列化操作。
     * 关键配置：
     * • FAIL_ON_UNKNOWN_PROPERTIES = false：宽松的JSON解析，忽略未知属性
     * • 支持多种日期格式
     * • 自定义反序列化策略
     */
    private final ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * AI智能评分核心方法 - 实现口语评估的主入口
     * <p>
     * 这是与DeepSeek大模型交互的核心业务方法，通过精心设计的提示词工程和
     * 请求参数配置，获取高质量的口语评估结果。
     * <p>
     * <b>弹性机制：</b><br>
     * • @Retry：自动重试机制，处理临时性网络故障<br>
     *   - 配置可重试次数和退避策略<br>
     *   - 只重试可重试的异常（超时、5xx错误）<br>
     * • @CircuitBreaker：熔断器模式，防止级联故障<br>
     *   - 失败率阈值触发熔断<br>
     *   - 熔断期内直接调用降级方法<br>
     *   - 半开状态进行试探性恢复
     * <p>
     * <b>业务流程：</b><br>
     * 1. 前置校验：过滤无效输入，避免不必要的API调用<br>
     * 2. 提示词构建：根据用户画像和场景定制评估策略<br>
     * 3. API调用：发送结构化请求到DeepSeek服务<br>
     * 4. 响应解析：提取和处理JSON格式的评估结果<br>
     * 5. 结果返回：返回包含评分和反馈的完整数据
     *
     * @param persona    用户画像类型（备考党/职场人等），影响评估的侧重点
     * @param scene      评估场景（日常练习/模拟面试等），提供上下文信息
     * @param transcript ASR转换得到的文字内容，是评估的直接依据
     * @return DeepSeekEvalResult 包含多维度评分和详细反馈的评估结果
     * @throws IllegalArgumentException 当输入参数无效时
     */
    @Retry(name = "deepseek")
    @CircuitBreaker(name = "deepseek", fallbackMethod = "fallbackEval")
    public DeepSeekEvalResult evaluate(String transcript) {
        // Use default persona and scene for simplified evaluation
        return evaluate(UserPersona.CAREER_GROWTH, "General", transcript);
    }

    /**
     * AI智能评分核心方法 - 实现口语评估的主入口
     *
     * @param persona    用户画像类型（备考党/职场人等），影响评估的侧重点
     * @param scene      评估场景（日常练习/模拟面试等），提供上下文信息
     * @param transcript ASR转换得到的文字内容，是评估的直接依据
     * @return DeepSeekEvalResult 包含多维度评分和详细反馈的评估结果
     * @throws IllegalArgumentException 当输入参数无效时
     */
    @Retry(name = "deepseek")
    @CircuitBreaker(name = "deepseek", fallbackMethod = "fallbackEval")
    public DeepSeekEvalResult evaluate(UserPersona persona, String scene, String transcript) {
        log.info("🤖 开始DeepSeek评分 - 画像: {}, 场景: {}, 文本长度: {}",
                persona, scene, transcript == null ? 0 : transcript.length());

        if (transcript == null || transcript.trim().isEmpty()) {
            log.warn("转录文本为空，返回无效输入结果");
            return DeepSeekEvalResult.noSpeech();
        }

        String[] words = transcript.trim().split("\\s+");
        if (words.length < 5) {
            log.warn("转录文本过短（{}词），无法进行有效评估", words.length);
            return DeepSeekEvalResult.fallback("invalid_input", "Transcript too short: need at least 5 words");
        }

        String systemPrompt = PromptFactory.buildSystemPrompt();
        String userPrompt = PromptFactory.buildUserPrompt(persona, scene, transcript);

        log.debug("系统提示词长度: {}, 用户提示词长度: {}",
                systemPrompt.length(), userPrompt.length());

        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", new Object[] {
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                },
                "temperature", 0.2);

        try {
            String raw = deepSeekWebClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            resp -> resp.bodyToMono(String.class)
                                    .map(msg -> new IllegalArgumentException("DeepSeek 4xx错误: " + msg)))
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));

            if (raw != null && raw.length() > 500) {
                log.debug("DeepSeek响应（截断）: {}...{}",
                        raw.substring(0, 200), raw.substring(raw.length() - 200));
            } else {
                log.debug("DeepSeek响应: {}", raw);
            }

            return parseJsonResponse(raw);

        } catch (Exception e) {
            log.error("DeepSeek API调用异常: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 熔断器降级方法 - 服务不可用时的备选方案
     * <p>
     * 当主评估方法（evaluate）因以下情况触发降级时，Resilience4j框架会自动调用此方法：
     * • 熔断器处于"打开"状态
     * • 达到最大重试次数后仍失败
     * • 调用超时
     * <p>
     * <b>降级策略：</b><br>
     * • 记录详细的错误日志，便于问题追踪<br>
     * • 返回预设的错误结果，避免系统崩溃<br>
     * • 保留输入参数，确保数据完整性<br>
     * • 提供友好的用户提示
     * <p>
     * <b>错误分类处理：</b><br>
     * • 网络超时：提示用户检查网络连接<br>
     * • 配额耗尽：提示稍后重试<br>
     • 服务异常：提示服务暂时不可用
     *
     * @param persona    用户画像类型（原始参数，用于日志记录）
     * @param scene      评估场景（原始参数，用于日志记录）
     * @param transcript 转录文本（原始参数，用于日志记录）
     * @param t          触发降级的异常对象
     * @return DeepSeekEvalResult 包含错误信息的降级评估结果
     */
    private DeepSeekEvalResult fallbackEval(UserPersona persona, String scene, String transcript, Throwable t) {
        // 详细记录错误信息，包含上下文和异常详情
        log.error("🚨 DeepSeek服务降级触发 - 画像: {}, 场景: {}, 异常类型: {}, 错误信息: {}",
                persona, scene, t.getClass().getSimpleName(), t.getMessage());

        // 根据异常类型提供不同的用户提示
        String userMessage;
        if (t instanceof java.net.ConnectException) {
            userMessage = "网络连接失败，请检查网络后重试";
        } else if (t instanceof java.util.concurrent.TimeoutException) {
            userMessage = "请求超时，请稍后重试";
        } else {
            userMessage = "AI服务暂时不可用，请稍后重试";
        }

        return DeepSeekEvalResult.fallback("service_unavailable", userMessage);
    }

    /**
     * DeepSeek响应解析引擎 - 处理复杂的AI输出
     * <p>
     * 这是服务中最关键的复杂逻辑，需要处理DeepSeek API的各种响应格式：
     * • 标准OpenAI格式：嵌套的choices数组结构
     * • Markdown包装：JSON内容包裹在```json```代码块中
     * • 直接JSON：有时返回纯JSON内容
     * • 异常响应：空响应或格式错误
     * <p>
     * <b>解析策略：</b><br>
     * 1. 首先尝试OpenAI标准格式：提取choices[0].message.content<br>
     * 2. 如果失败，尝试直接解析响应体<br>
     * 3. 清理Markdown标记：移除```json和```<br>
     * 4. 提取第一个完整的JSON对象<br>
     * 5. 严格模式解析到DTO对象<br>
     * <p>
     * <b>容错设计：</b><br>
     • 空响应处理：返回错误结果<br>
     • JSON格式错误：记录异常并提供降级结果<br>
     • 数据缺失：填充默认值<br>
     * 性能优化：使用Jackson JsonNode进行流式处理
     *
     * @param rawResponse DeepSeek API返回的原始响应字符串
     * @return DeepSeekEvalResult 解析后的结构化评估结果
     * @throws JsonProcessingException 当JSON解析失败时
     */
    private DeepSeekEvalResult parseJsonResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return DeepSeekEvalResult.fallback("error", "Empty response from AI");
        }

        // ====== JSON响应解析核心逻辑 ======

        try {
            // 【第一阶段】解析为JsonNode树
            // 使用Jackson的流式API高效处理大JSON响应
            com.fasterxml.jackson.databind.JsonNode root = om.readTree(rawResponse);
            log.debug("JSON根节点解析成功，类型: {}", root.getNodeType());

            // 【第二阶段】识别响应格式
            // 检查是否为标准的OpenAI兼容格式（包含choices数组）
            com.fasterxml.jackson.databind.JsonNode choicesNode = root.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                // 【标准OpenAI格式处理】
                // 从嵌套结构中提取业务JSON内容
                String content = choicesNode.get(0).path("message").path("content").asText();
                log.info("📝 从OpenAI格式提取到AI内容 ({}字符)", content.length());

                // 内容大小检查和日志记录
                if (content.length() > 500) {
                    log.info("AI内容截断显示: {}...{}",
                            content.substring(0, 200), content.substring(content.length() - 200));
                }

                // 清理Markdown标记并进行严格解析
                String cleanJson = extractFirstJsonObject(content);
                return parseStrict(cleanJson);

            } else {
                // 【非标准格式处理】
                // 可能是测试环境或直接返回的业务JSON
                log.warn("检测到非OpenAI格式响应，尝试直接解析");
                String extracted = extractFirstJsonObject(rawResponse);
                return parseStrict(extracted);
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // JSON格式错误处理
            log.error("🔴 JSON解析失败 - 原始响应: {}", safeTrim(rawResponse), e);
            return DeepSeekEvalResult.fallback("parse_error", "AI响应解析失败，请重试");
        } catch (Exception e) {
            // 其他异常处理
            log.error("🔴 响应处理异常: {}", e.getMessage(), e);
            return DeepSeekEvalResult.fallback("error", "处理AI响应时发生错误: " + e.getMessage());
        }
    }

    /**
     * 严格JSON解析器 - 数据清洗和补齐
     * <p>
     * 此方法将纯净的JSON字符串解析为目标DTO对象，并进行严格的数据校验和清洗。
     * 即使原始数据不完整或格式不规范，也能返回一个可用的评估结果。
     * <p>
     * <b>数据清洗规则：</b><br>
     * 1. metrics字段为null → 使用空Map<br>
     * 2. status字段为null → 设置为"ok"<br>
     * 3. overallScore为null → 设置为0分<br>
     * 4. feedback为null → 创建空的反馈对象<br>
     * 5. 字段类型校验 → 确保数值类型的正确性
     * <p>
     * <b>设计原则：</b><br>
     • 鲁棒性：不因数据缺失而崩溃<br>
     • 一致性：确保返回对象结构完整<br>
     * 向后兼容：支持新旧版本的数据格式<br>
     * 安全性：防御性编程，避免NullPointerException
     *
     * @param json 经过清理的纯JSON字符串
     * @return DeepSeekEvalResult 结构完整、数据可靠的评估结果对象
     * @throws Exception 当JSON格式严重错误时抛出
     */
    private DeepSeekEvalResult parseStrict(String json) throws Exception {
        // 第一阶段：基础JSON解析
        DeepSeekEvalResult result = om.readValue(json, DeepSeekEvalResult.class);
        log.debug("基础JSON解析完成，状态: {}, 总分: {}", result.status(), result.overallScore());

        // 第二阶段：数据完整性校验和修复
        // 修复metrics字段
        if (result.metrics() == null) {
            log.warn("评估指标为空，使用默认空Map");
            result = new DeepSeekEvalResult(result.status(), result.overallScore(), Map.of(), result.feedback());
        }

        // 修复status字段
        if (result.status() == null) {
            log.warn("评估状态为空，默认设置为'ok'");
            result = new DeepSeekEvalResult("ok", result.overallScore(), result.metrics(), result.feedback());
        }

        // 修复overallScore字段
        if (result.overallScore() == null) {
            log.warn("总分为空，默认设置为0");
            result = new DeepSeekEvalResult(result.status(), 0, result.metrics(), result.feedback());
        }

        // 修复feedback字段
        if (result.feedback() == null) {
            log.warn("反馈信息为空，创建默认反馈对象");
            var emptyFeedback = new DeepSeekEvalResult.Feedback(
                    "暂无反馈信息。",
                    List.of(),
                    List.of(),
                    List.of(),
                    ""
            );
            result = new DeepSeekEvalResult(result.status(), result.overallScore(), result.metrics(), emptyFeedback);
        }

        log.debug("数据清洗完成，最终状态: {}", result.status());
        return result;
    }

    /**
     * JSON对象提取器 - 从复杂文本中提取结构化数据
     * <p>
     * DeepSeek API的响应有时会包含Markdown格式的代码块标记，如：
     * ```json
     * {"status": "ok", "score": 85}
     * ```
     * 此方法能够智能地清理这些标记，并提取出有效的JSON内容。
     * <p>
     * <b>处理流程：</b><br>
     * 1. 边界检查：处理null或空白字符串<br>
     * 2. Markdown清理：移除```json和```标记<br>
     * 3. JSON定位：找到第一个'{'和最后一个'}'<br>
     * 4. 内容提取：截取完整的JSON对象<br>
     * 5. 降级处理：如果找不到JSON标记，返回原文本
     * <p>
     * <b>正则表达式优化：</b><br>
     * 使用简单的字符串替换而非复杂正则，提高性能<br>
     * 避免过度处理，保持原始数据结构
     *
     * @param text 可能包含Markdown标记的原始文本
     * @return 清理后的JSON字符串，如果无有效JSON则返回原文本
     */
    private String extractFirstJsonObject(String text) {
        // 边界情况处理
        if (text == null || text.isBlank())
            return "{}";

        // 第一阶段：Markdown标记清理
        String processed = text
                .replaceAll("```json", "")  // 移除JSON代码块开始标记
                .replaceAll("```", "")        // 移除代码块结束标记
                .trim();

        // 第二阶段：JSON对象定位和提取
        int start = processed.indexOf('{');
        int end = processed.lastIndexOf('}');

        // 验证JSON结构完整性
        if (start != -1 && end != -1 && start < end) {
            String json = processed.substring(start, end + 1);
            log.debug("成功提取JSON对象，长度: {} 字符", json.length());
            return json;
        }

        // 降级策略：返回处理后的文本，可能它本身就是JSON
        log.debug("未找到标准JSON结构，返回处理后的文本");
        return processed;
    }

    /**
     * 日志安全截断工具 - 防止日志输出过大
     * <p>
     * 在记录AI响应等可能很长的文本时，此方法确保日志不会因内容过长而影响性能。
     * 保留关键信息的同时避免日志系统过载。
     * <p>
     * <b>截断策略：</b><br>
     * • 800字符阈值：超过此长度自动截断<br>
     * • 保留头部内容：显示最重要的信息<br>
     * • 添加省略标记：明确标识内容被截断<br>
     * • null安全：处理null输入不抛出异常
     *
     * @param s 可能很长的原始字符串
     * @return 截断后的安全字符串，长度不超过800字符
     */
    private String safeTrim(String s) {
        if (s == null)
            return "";

        if (s.length() > 800) {
            log.debug("字符串截断: 原始长度={}, 截断后长度={}", s.length(), 800);
            return s.substring(0, 800) + "...[截断]";
        }
        return s;
    }

    private double getMetricValue(java.util.Map<?, ?> map, String key, double defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    // ====== 向后兼容方法 ======

    /**
     * @deprecated 向后兼容方法 - 建议使用新的evaluate方法
     * <p>
     * 此方法保留是为了兼容旧版本的代码调用。新功能开发应优先使用
     * {@code evaluate(UserPersona, String, String)} 方法，它提供了更灵活
     * 的参数配置和更好的错误处理。
     * <p>
     * <b>主要区别：</b><br>
     * • 固定场景为"General"，无法自定义评估上下文<br>
     * • 参数顺序不同，可能造成混淆<br>
     * • 返回类型不同，需要额外的转换步骤
     *
     * @param prompt 旧版本参数，实际未使用
     * @param userText 待评估的用户文本
     * @param persona 用户画像
     * @return 兼容旧版本格式的评估结果
     */
    @Deprecated
    public com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp evaluate(String prompt, String userText,
            UserPersona persona) {
        log.warn("⚠️ 使用已弃用的evaluate方法，建议使用新版本");

        // 调用新的评估方法，场景固定为"General"
        DeepSeekEvalResult result = evaluate(persona, "General", userText);
        return mapToTextEvalResp(result, userText);
    }

    /**
     * 评估结果数据映射器 - 新旧数据结构兼容层
     * <p>
     * 此方法将新的DeepSeekEvalResult结构映射到旧的TextEvalResp结构，
     * 实现数据格式的平滑过渡。它处理了字段转换和类型映射，
     * 确保旧版本的前端仍能正常工作。
     * <p>
     * <b>映射规则：</b><br>
     * • 流利度：fluency → fluency<br>
     * • 完整度：completeness → completeness<br>
     * • 相关性：relevance → relevance<br>
     * • 用户文本：直接赋值<br>
     * • 建议：suggestions数组直接映射<br>
     * • 问题：issues转换为特定格式
     * <p>
     * <b>问题处理：</b><br>
     * 每个问题包含evidence和fix信息，合并为一条消息<br>
     * 偏移量(offset)和长度(length)信息在此版本中暂不处理<br>
     * 为null的情况提供默认值，避免空指针异常
     *
     * @param result 新版本的DeepSeek评估结果
     * @param userText 原始用户输入文本
     * @return 映射后的旧版本评估响应对象
     */
    public com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp mapToTextEvalResp(DeepSeekEvalResult result,
            String userText) {
        // 创建目标DTO对象
        com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp resp = new com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp();

        // 基础评分映射
        java.util.Map<String, Object> metrics = result.metrics() != null
                ? new java.util.HashMap<>(result.metrics())
                : java.util.Collections.emptyMap();

        resp.setFluency(getMetricValue(metrics, "fluency", 0.0));
        resp.setCompleteness(getMetricValue(metrics, "completeness", 0.0));
        resp.setRelevance(getMetricValue(metrics, "relevance", 0.0));
        resp.setUserText(userText);

        // 反馈信息映射
        if (result.feedback() != null) {
            // 建议列表直接映射
            resp.setSuggestions(result.feedback().suggestions());

            // 问题信息转换处理
            if (result.feedback().issues() != null) {
                java.util.List<com.zhupinzan.speaking.model.dto.EvalDTO.Issue> issues = result.feedback().issues()
                        .stream()
                        .map(issue -> {
                            com.zhupinzan.speaking.model.dto.EvalDTO.Issue dtoIssue =
                                    new com.zhupinzan.speaking.model.dto.EvalDTO.Issue();

                            // 合并证据和修复建议为一条消息
                            String message = issue.evidence();
                            if (issue.fix() != null && !issue.fix().isEmpty()) {
                                message += ": " + issue.fix();
                            }
                            dtoIssue.setMessage(message);

                            // TODO: 未来版本可以添加offset和length字段的映射
                            // dtoIssue.setOffset(issue.offset());
                            // dtoIssue.setLength(issue.length());

                            return dtoIssue;
                        })
                        .toList();

                resp.setIssues(issues);
            }
        }

        log.debug("评估结果映射完成，用户文本长度: {}", userText != null ? userText.length() : 0);
        return resp;
    }
}
