package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.AssessmentResult; // 之前定义的 AI 返回结果类
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.IOException;

/**
 * 评估服务类，负责处理口语评估的业务逻辑，包括调用AI服务和保存评估记录
 */
@Service
public class AssessmentService {

    /** DeepSeek服务，用于调用AI评估 */
    private final DeepSeekService deepSeekService;
    /** 评估记录仓库，用于数据库操作 */
    private final AssessmentRecordRepository recordRepository;

    /** 构造函数，注入依赖 */
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
        
        // 步骤1: 调用 DeepSeek 获取评分
        AssessmentResult aiResult = deepSeekService.evaluateText(text, scenarioName);

        // 步骤2: 将 AI 结果映射到数据库实体
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setScenarioId(scenarioId);
        record.setTranscribedText(text);
        
        // 设置分数 (注意 BigDecimal 转换)
        record.setScoreTotal(aiResult.getTotalScore());
        
        // 如果维度不为空，设置各项评分
        if (aiResult.getDimensions() != null) {
            record.setScoreFluency(aiResult.getDimensions().getVocabulary()); // 词汇分
            record.setScoreIntegrity(aiResult.getDimensions().getLogic());   // 逻辑分
            record.setScorePronunciation(aiResult.getDimensions().getGrammar()); // 语法分
        }

        // 保存原始 AI JSON 数据，方便前端展示具体的“语法建议”和“润色文本”
        record.setAiAnalysisRaw(JSON.toJSONString(aiResult));

        // 步骤3: 存入数据库
        return recordRepository.save(record);
    }
}