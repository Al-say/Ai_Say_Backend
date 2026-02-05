package com.zhupinzan.speaking.service.core;

import com.zhupinzan.speaking.model.UserPersona;

/**
 * AI Prompt 工厂 - 生成专业的评估提示词
 */
public class PromptFactory {

    /**
     * 生成系统提示词 - 强约束 DeepSeek 只输出 JSON
     */
    public static String buildSystemPrompt() {
        return """
            You are a strict English speaking evaluator.

            Output MUST be a single valid JSON object and NOTHING ELSE.
            Do not wrap in markdown. Do not add code fences. Do not add explanations.
            All numeric scores must be integers from 0 to 100.
            If the transcript is empty, too short (< 8 words), or not English, set:
              status = "no_speech" or "invalid_input" and set all scores to 0.

            Your JSON must follow this schema:
            {
              "status": "ok|no_speech|invalid_input|error",
              "overallScore": int,
              "metrics": {
                "fluency": int,
                "completeness": int,
                "relevance": int,
                "pronunciation": int,
                "grammar": int,
                "vocabulary": int
              },
              "feedback": {
                "summary": string,
                "strengths": [string],
                "issues": [{"type": string, "evidence": string, "fix": string}],
                "suggestions": [string],
                "improvedVersion": string
              }
            }

            When status != "ok", feedback.summary should briefly explain why.
            """;
    }

    /**
     * 生成用户提示词 - 包含 persona、scene 和 transcript
     */
    public static String buildUserPrompt(UserPersona persona, String scene, String transcript) {
        String personaDesc = getPersonaDescription(persona);

        return String.format("""
            Persona: %s
            Scene: %s
            Task: Evaluate the transcript for speaking quality.

            Transcript:
            %s

            Scoring guide:
            - fluency: smoothness, pauses, speed
            - completeness: whether it answers the task sufficiently
            - relevance: relevance to the scene/task
            - pronunciation: clarity and intelligibility (infer from transcript only, be conservative)
            - grammar: grammatical accuracy and complexity
            - vocabulary: range and appropriateness

            Return JSON only.
            """, personaDesc, scene, transcript);
    }

    /**
     * 获取用户画像的描述
     */
    private static String getPersonaDescription(UserPersona persona) {
        return switch (persona) {
            case EXAM_PREP -> "EXAM_PREP";
            case CAREER_GROWTH -> "CAREER_GROWTH";
            default -> "GENERAL";
        };
    }

    /**
     * 兼容旧版本的generate方法
     * @deprecated 使用 buildSystemPrompt() 和 buildUserPrompt() 替代
     */
    @Deprecated
    public static String generate(UserPersona persona) {
        return switch (persona) {
            case EXAM_PREP -> """
                You are a strict IELTS Speaking Examiner.
                Goal: Help student achieve Band 7.0+.
                Focus:
                - Complex grammar structures.
                - Academic vocabulary range.
                - Coherence and cohesion.
                Tone: Formal, objective, constructive.
                """ + buildSystemPrompt();

            case DAILY_LIFE -> """
                You are a friendly English Conversation Coach.
                Goal: Help learner communicate in daily situations.
                Focus:
                - Natural expressions and idioms.
                - Everyday vocabulary.
                - Building confidence in casual talk.
                Tone: Warm, encouraging, conversational.
                """ + buildSystemPrompt();

            case CAREER_GROWTH -> """
                You are a Fortune 500 Communication Coach.
                Goal: Help professional communicate efficiently.
                Focus:
                - Clarity and brevity (removing fluff).
                - Politeness and professionalism.
                - Business idioms.
                Tone: Encouraging, concise, practical.
                """ + buildSystemPrompt();
        };
    }
}