package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 评估服务类，用于处理文本评估请求，调用AI服务并保存评估记录
 */
@Service
@RequiredArgsConstructor
public class EvalService {

    /** DeepSeek服务，用于调用AI评估 */
    private final DeepSeekService deepSeekService;
    /** 评估记录仓库，用于数据库操作 */
    private final AssessmentRecordRepository recordRepository;

    /**
     * 评估方法，处理文本评估请求
     * @param req 评估请求DTO
     * @return 评估响应DTO
     * @throws Exception 如果评估失败
     */
    public EvalDTO.TextEvalResp evaluate(EvalDTO.TextEvalReq req) throws Exception {
        
        // 步骤1: 构建 System Prompt (契约的核心)
        // 这里的 JSON 示例必须和 iOS 前端的结构 1:1 对应
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

        String userPrompt = String.format("【题目】%s\n【回答】%s", req.getPrompt(), req.getUserText());

        // 步骤2: 调用 DeepSeek (假设 deepSeekService.chat 返回纯字符串)
        String jsonStr = deepSeekService.chat(systemPrompt, userPrompt);
        
        // 清洗数据：防止 AI 偶尔带上 Markdown 标记
        jsonStr = jsonStr.replace("```json", "").replace("```", "").trim();

        // 步骤3: 解析为 DTO
        EvalDTO.TextEvalResp resp = JSON.parseObject(jsonStr, EvalDTO.TextEvalResp.class);

        // 步骤4: 入库 (映射 DTO -> Entity)
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(1L); // 暂时硬编码
        record.setTranscribedText(req.getUserText());
        record.setScoreFluency(BigDecimal.valueOf(resp.getFluency()));
        record.setScoreIntegrity(BigDecimal.valueOf(resp.getCompleteness()));
        // 总分简单取平均
        double total = (resp.getFluency() + resp.getCompleteness() + resp.getRelevance()) / 3.0;
        record.setScoreTotal(BigDecimal.valueOf(total));
        record.setAiAnalysisRaw(jsonStr); // 保存原始 JSON
        
        record = recordRepository.save(record);

        // 步骤5: 补充返回字段 (ID 和 时间)
        resp.setRecordId(record.getRecordId());
        resp.setCreatedAt(record.getCreatedAt());

        return resp;
    }
}