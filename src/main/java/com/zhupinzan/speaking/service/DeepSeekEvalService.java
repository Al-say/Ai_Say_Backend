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
import java.util.Map;

/**
 * DeepSeek 评估服务 - 使用 WebClient + Resilience4j 实现超时/重试/熔断
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeepSeekEvalService {

    private final WebClient deepSeekWebClient;
    private final ObjectMapper om = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Retry(name = "deepseek")
    @CircuitBreaker(name = "deepseek", fallbackMethod = "fallbackEval")
    public DeepSeekEvalResult evaluate(UserPersona persona, String scene, String transcript) {
        log.info("🤖 调用 DeepSeek: persona={}, scene={}, transcriptLen={}", persona, scene,
                transcript == null ? 0 : transcript.length());

        // 前置校验
        if (transcript == null || transcript.trim().isEmpty())
            return DeepSeekEvalResult.noSpeech();
        if (transcript.trim().split("\\s+").length < 5)
            return DeepSeekEvalResult.fallback("invalid_input", "Transcript too short");

        String systemPrompt = PromptFactory.buildSystemPrompt();
        String userPrompt = PromptFactory.buildUserPrompt(persona, scene, transcript);

        Map<String, Object> body = Map.of(
                "model", "deepseek-chat",
                "messages", new Object[] {
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                },
                "temperature", 0.2);

        String raw = deepSeekWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        resp -> resp.bodyToMono(String.class)
                                .map(msg -> new IllegalArgumentException("DeepSeek 4xx: " + msg)))
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(30)); // overall block timeout

        log.debug("📡 DeepSeek 原始响应: {}",
                raw == null ? "<null>" : (raw.length() > 500 ? raw.substring(0, 500) + "..." : raw));

        // 解析并返回
        return parseJsonResponse(raw);
    }

    // CircuitBreaker fallback（签名必须匹配：原参数 + Throwable）
    private DeepSeekEvalResult fallbackEval(UserPersona persona, String scene, String transcript, Throwable t) {
        log.error("DeepSeek call failed (fallback). persona={}, scene={}, errorType={}, errorMsg={}",
                persona, scene, t.getClass().getSimpleName(), t.getMessage());
        return DeepSeekEvalResult.fallback("error", "AI service temporarily unavailable: " + t.getMessage());
    }

    // 解析逻辑：先从 OpenAI 格式提取 content，再解析业务 JSON
    private DeepSeekEvalResult parseJsonResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            return DeepSeekEvalResult.fallback("error", "Empty response from AI");
        }

        try {
            // 🔑 关键修复：从 OpenAI 格式响应中提取 choices[0].message.content
            com.fasterxml.jackson.databind.JsonNode root = om.readTree(rawResponse);
            com.fasterxml.jackson.databind.JsonNode choicesNode = root.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                String content = choicesNode.get(0).path("message").path("content").asText();
                log.info("📝 提取到 AI 内容 ({}字): {}", content.length(),
                        content.length() > 200 ? content.substring(0, 200) + "..." : content);

                // 清理 Markdown 并解析业务 JSON
                String cleanJson = extractFirstJsonObject(content);
                return parseStrict(cleanJson);
            } else {
                // 可能直接就是业务 JSON（用于测试）
                log.warn("响应不是 OpenAI 格式，尝试直接解析");
                String extracted = extractFirstJsonObject(rawResponse);
                return parseStrict(extracted);
            }
        } catch (Exception e) {
            log.error("JSON 解析失败, raw={}", safeTrim(rawResponse), e);
            return DeepSeekEvalResult.fallback("error", "Failed to parse AI response: " + e.getMessage());
        }
    }

    private DeepSeekEvalResult parseStrict(String json) throws Exception {
        DeepSeekEvalResult result = om.readValue(json, DeepSeekEvalResult.class);

        // 补齐缺失字段
        if (result.metrics() == null) {
            result = new DeepSeekEvalResult(result.status(), result.overallScore(), Map.of(), result.feedback());
        }

        if (result.status() == null) {
            result = new DeepSeekEvalResult("ok", result.overallScore(), result.metrics(), result.feedback());
        }

        return result;
    }

    private String extractFirstJsonObject(String text) {
        if (text == null || text.isBlank())
            return "{}";

        // 1. 移除常见的 Markdown 标识符
        String processed = text.replaceAll("```json", "").replaceAll("```", "").trim();

        // 2. 查找第一个 { 和最后一个 }
        int start = processed.indexOf('{');
        int end = processed.lastIndexOf('}');

        if (start != -1 && end != -1 && start < end) {
            return processed.substring(start, end + 1);
        }
        return processed;
    }

    private String safeTrim(String s) {
        if (s == null)
            return "";
        return s.length() > 800 ? s.substring(0, 800) + "..." : s;
    }

    // 保留旧的兼容方法（简化实现）
    @Deprecated
    public com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp evaluate(String prompt, String userText,
            UserPersona persona) {
        DeepSeekEvalResult result = evaluate(persona, "General", userText);
        return mapToTextEvalResp(result, userText);
    }

    public com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp mapToTextEvalResp(DeepSeekEvalResult result,
            String userText) {
        com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp resp = new com.zhupinzan.speaking.model.dto.EvalDTO.TextEvalResp();
        resp.setFluency(result.metrics().getOrDefault("fluency", 0).doubleValue());
        resp.setCompleteness(result.metrics().getOrDefault("completeness", 0).doubleValue());
        resp.setRelevance(result.metrics().getOrDefault("relevance", 0).doubleValue());
        resp.setUserText(userText);

        if (result.feedback() != null) {
            resp.setSuggestions(result.feedback().suggestions());
            if (result.feedback().issues() != null) {
                resp.setIssues(result.feedback().issues().stream().map(issue -> {
                    com.zhupinzan.speaking.model.dto.EvalDTO.Issue dtoIssue = new com.zhupinzan.speaking.model.dto.EvalDTO.Issue();
                    dtoIssue.setMessage(issue.evidence() + ": " + issue.fix());
                    // offset and length might need more complex logic if available, here we just
                    // set the message
                    return dtoIssue;
                }).toList());
            }
        }
        return resp;
    }
}