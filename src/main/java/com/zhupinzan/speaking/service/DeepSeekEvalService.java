package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * DeepSeek 评估服务，用于调用 DeepSeek API 进行文本评估
 */
@Service
@RequiredArgsConstructor
public class DeepSeekEvalService {

    private final DeepSeekService deepSeekService;

    /**
     * 评估用户回答文本
     * @param prompt 题目
     * @param userText 用户回答文本
     * @return 评估结果
     * @throws Exception 评估失败时抛出
     */
    public EvalDTO.TextEvalResp evaluateText(String prompt, String userText) throws Exception {
        // 构建系统提示
        String systemPrompt = """
            你是一位专业的雅思口语考官。请根据【题目】评估用户的【回答】。

            【输出要求】
            必须严格返回如下 JSON 格式，不要包含 Markdown 标记（如 ```json），不要包含其他废话：
            {
                "fluency": 85.0,        // 流利度/词汇丰富度 (0-100)
                "completeness": 80.0,   // 完整度 (0-100)
                "relevance": 90.0,      // 相关性 (0-100)
                "grammarIssueCount": 1, // 语法错误数
                "issues": [             // 具体错误列表
                    { "offset": 2, "length": 4, "message": "时态错误", "replacements": ["went"] }
                ],
                "suggestions": ["建议1", "建议2"], // 改进建议
                "missingKeywords": []   // 遗漏的关键点
            }
            注意：offset 是错误单词在原句中的起始索引（从0开始）。
            """;

        String userPrompt = String.format("【题目】%s\n【回答】%s", prompt, userText);

        // 调用 DeepSeek
        String jsonStr = deepSeekService.chat(systemPrompt, userPrompt);

        // 清洗数据
        jsonStr = jsonStr.replace("```json", "").replace("```", "").trim();

        // 解析为 DTO
        return JSON.parseObject(jsonStr, EvalDTO.TextEvalResp.class);
    }
}