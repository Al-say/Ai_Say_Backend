package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.service.core.PromptFactory;
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
     * @param persona 用户画像
     * @return 评估结果
     * @throws Exception 评估失败时抛出
     */
    public EvalDTO.TextEvalResp evaluate(String prompt, String userText, UserPersona persona) throws Exception {
        
        // 1. 动态获取 System Prompt
        String systemPrompt = PromptFactory.generate(persona);
        
        // 2. 组装 User Prompt
        String userPrompt = "Topic: " + prompt + "\nStudent Answer: " + userText;
        
        // 3. 调用 API (保持原有逻辑，只是 systemPrompt 变了)
        String jsonStr = deepSeekService.chat(systemPrompt, userPrompt);

        // 清洗数据
        jsonStr = jsonStr.replace("```json", "").replace("```", "").trim();

        // 解析为 DTO
        return JSON.parseObject(jsonStr, EvalDTO.TextEvalResp.class);
    }
}