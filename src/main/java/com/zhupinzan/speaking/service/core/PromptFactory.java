package com.zhupinzan.speaking.service.core;

import com.zhupinzan.speaking.model.UserPersona;

public class PromptFactory {

    // ⚠️ 核心契约：无论 Prompt 怎么变，输出 JSON 格式必须锁死
    private static final String JSON_SCHEMA_RULE = """
        CRITICAL OUTPUT RULES:
        1. Return ONLY raw JSON. No markdown.
        2. Schema:
        {
            "fluency": 0-100,
            "completeness": 0-100,
            "relevance": 0-100,
            "issues": [{"offset": int, "length": int, "message": "string", "replacements": ["string"]}],
            "suggestions": ["string"]
        }
        """;

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
                """ + JSON_SCHEMA_RULE;

            case CAREER_GROWTH -> """
                You are a Fortune 500 Communication Coach.
                Goal: Help professional communicate efficiently.
                Focus:
                - Clarity and brevity (removing fluff).
                - Politeness and professionalism.
                - Business idioms.
                Tone: Encouraging, concise, practical.
                """ + JSON_SCHEMA_RULE;
        };
    }
}