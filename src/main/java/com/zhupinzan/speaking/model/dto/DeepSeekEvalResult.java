package com.zhupinzan.speaking.model.dto;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek 评估结果 - 强类型 JSON Schema
 */
public record DeepSeekEvalResult(
    String status,
    Integer overallScore,
    Map<String, Integer> metrics,
    Feedback feedback
) {
    public record Feedback(
        String summary,
        List<String> strengths,
        List<Issue> issues,
        List<String> suggestions,
        String improvedVersion
    ) {}

    public record Issue(String type, String evidence, String fix) {}

    /**
     * 创建错误状态的降级结果
     */
    public static DeepSeekEvalResult fallback(String reason) {
        return new DeepSeekEvalResult(
            "error",
            0,
            Map.of(
                "fluency", 0,
                "completeness", 0,
                "relevance", 0,
                "pronunciation", 0,
                "grammar", 0,
                "vocabulary", 0
            ),
            new Feedback(
                "Evaluation failed: " + reason,
                List.of(),
                List.of(),
                List.of("Try again later."),
                ""
            )
        );
    }

    /**
     * 更通用的 fallback，允许指定 status 和消息
     */
    public static DeepSeekEvalResult fallback(String status, String msg) {
        return new DeepSeekEvalResult(
            status,
            0,
            Map.of(
                "fluency", 0,
                "completeness", 0,
                "relevance", 0,
                "pronunciation", 0,
                "grammar", 0,
                "vocabulary", 0
            ),
            new Feedback(
                msg,
                List.of(),
                List.of(),
                List.of(),
                ""
            )
        );
    }

    /**
     * 创建无语音输入的结果
     */
    public static DeepSeekEvalResult noSpeech() {
        return new DeepSeekEvalResult(
            "no_speech",
            0,
            Map.of(
                "fluency", 0,
                "completeness", 0,
                "relevance", 0,
                "pronunciation", 0,
                "grammar", 0,
                "vocabulary", 0
            ),
            new Feedback(
                "No speech detected in the audio. Please speak clearly and try again.",
                List.of(),
                List.of(),
                List.of("Ensure your microphone is working", "Speak louder and more clearly"),
                ""
            )
        );
    }
}