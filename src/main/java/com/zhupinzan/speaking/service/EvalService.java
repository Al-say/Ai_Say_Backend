package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.zhupinzan.speaking.model.AssessmentResult;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvalService {

    private final DeepSeekService deepSeekService;
    private final AssessmentRecordRepository recordRepository;

    public EvalService(DeepSeekService deepSeekService, AssessmentRecordRepository recordRepository) {
        this.deepSeekService = deepSeekService;
        this.recordRepository = recordRepository;
    }

    @Transactional
    public EvalDTO.TextEvalResp evaluate(EvalDTO.TextEvalReq req) throws IOException {
        // 调用DeepSeek
        AssessmentResult aiResult = deepSeekService.evaluateText(req.getUserText(), req.getPrompt());

        // 映射到数据库实体
        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(1L); // 硬编码
        record.setScenarioId(101L); // 硬编码
        record.setTranscribedText(req.getUserText());
        record.setScoreTotal(aiResult.getTotalScore());
        record.setScoreFluency(aiResult.getDimensions().getVocabulary());
        record.setScoreIntegrity(aiResult.getDimensions().getLogic());
        record.setScorePronunciation(aiResult.getDimensions().getGrammar());
        record.setAiAnalysisRaw(JSON.toJSONString(aiResult));

        // 保存
        AssessmentRecord saved = recordRepository.save(record);

        // 构建响应
        EvalDTO.TextEvalResp resp = new EvalDTO.TextEvalResp();
        resp.setRecordId(saved.getRecordId());
        resp.setFluency(saved.getScoreFluency());
        resp.setCompleteness(saved.getScoreIntegrity());
        resp.setRelevance(saved.getScorePronunciation()); // 假设相关性用语法分
        resp.setGrammarIssueCount(aiResult.getGrammarErrors() != null ? aiResult.getGrammarErrors().size() : 0);
        if (aiResult.getGrammarErrors() != null) {
            List<EvalDTO.Issue> issues = aiResult.getGrammarErrors().stream()
                    .map(error -> {
                        EvalDTO.Issue issue = new EvalDTO.Issue();
                        issue.setMessage(error);
                        issue.setReplacements(""); // 简化
                        return issue;
                    })
                    .collect(Collectors.toList());
            resp.setIssues(issues);
        }
        resp.setSuggestions(aiResult.getSuggestions());

        return resp;
    }
}