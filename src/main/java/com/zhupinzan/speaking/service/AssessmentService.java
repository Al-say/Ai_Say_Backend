package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.AssessmentResult; // 之前定义的 AI 返回结果类
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.IOException;

@Service
public class AssessmentService {

    private final DeepSeekService deepSeekService;
    private final AssessmentRecordRepository recordRepository;

    public AssessmentService(DeepSeekService deepSeekService, AssessmentRecordRepository recordRepository) {
        this.deepSeekService = deepSeekService;
        this.recordRepository = recordRepository;
    }

    /**
     * 处理文本评测请求
     * @param userId 用户ID
     * @param scenarioId 场景ID
     * @param scenarioName 场景名称 (发给AI用)
     * @param text 用户输入的文本
     * @return 评测记录实体
     */
    @Transactional
    public AssessmentRecord evaluateText(Long userId, Long scenarioId, String scenarioName, String text) throws IOException {
        
        // 1. 调用 DeepSeek 获取评分
        AssessmentResult aiResult = deepSeekService.evaluateText(text, scenarioName);

        // 2. 将 AI 结果映射到数据库实体
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setScenarioId(scenarioId);
        record.setTranscribedText(text);
        
        // 设置分数 (注意 BigDecimal 转换)
        record.setScoreTotal(aiResult.getTotalScore());
        
        if (aiResult.getDimensions() != null) {
            record.setScoreFluency(aiResult.getDimensions().getVocabulary()); // 词汇分
            record.setScoreIntegrity(aiResult.getDimensions().getLogic());   // 逻辑分
            record.setScorePronunciation(aiResult.getDimensions().getGrammar()); // 语法分
        }

        // 保存原始 AI JSON 数据，方便前端展示具体的“语法建议”和“润色文本”
        record.setAiAnalysisRaw(JSON.toJSONString(aiResult));

        // 3. 存入数据库
        return recordRepository.save(record);
    }
}