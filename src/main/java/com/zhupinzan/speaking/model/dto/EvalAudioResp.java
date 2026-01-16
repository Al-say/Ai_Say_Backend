package com.zhupinzan.speaking.model.dto;

/**
 * 音频评估响应
 */
public record EvalAudioResp(
    Long recordId,
    String audioUrl,
    long durationMs,
    String transcript,
    int overallScore,
    int fluency,
    int completeness,
    int relevance,
    String feedback
) {}