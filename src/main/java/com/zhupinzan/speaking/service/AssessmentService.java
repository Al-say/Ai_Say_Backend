package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 评估数据服务类，负责保存和查询评估记录
 * 支持Growth模块的数据统计需求
 */
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentRecordRepository repository;

    /**
     * 保存评估尝试记录
     * @param deviceId 设备ID
     * @param persona 用户画像
     * @param prompt 提示词
     * @param userText 用户文本
     * @param audioUrl 音频URL
     * @param result 评估结果
     * @param isAudio 是否为音频模式
     * @return 保存的记录
     */
    public AssessmentRecord saveAttempt(String deviceId, UserPersona persona, String prompt,
                                      String userText, String audioUrl, EvalDTO.TextEvalResp result, boolean isAudio) {

        AssessmentRecord record = new AssessmentRecord();
        record.setDeviceId(deviceId);
        record.setPersona(persona);
        record.setPrompt(prompt);
        record.setTranscript(userText); // 改为transcript字段
        record.setAudioUrl(audioUrl);
        record.setMode(isAudio ? AssessmentMode.AUDIO : AssessmentMode.TEXT);
        record.setScene("practice"); // 默认值，未来可从 Controller 传

        // 分数落库 (扁平列)
        record.setFluency(result.getFluency());
        record.setCompleteness(result.getCompleteness());
        record.setRelevance(result.getRelevance());
        record.setOverallScore(result.getOverallScore());

        // 反馈信息落库 (合并到feedback JSONB)
        if (record.getFeedback() == null) {
            record.setFeedback(new java.util.HashMap<>());
        }

        if (result.getIssues() != null) {
            List<Map<String, Object>> issuesMaps = result.getIssues().stream()
                .map(issue -> Map.of(
                    "offset", issue.getOffset(),
                    "length", issue.getLength(),
                    "message", issue.getMessage(),
                    "replacements", issue.getReplacements()
                ))
                .collect(java.util.stream.Collectors.toList());
            record.getFeedback().put("issues", issuesMaps);
        }

        if (result.getSuggestions() != null) {
            record.getFeedback().put("suggestions", result.getSuggestions());
        }

        AssessmentRecord saved = repository.save(record);

        // 回填ID和时间给前端
        result.setRecordId(saved.getId());
        result.setCreatedAt(saved.getCreatedAt().toString());

        return saved;
    }

    /**
     * 获取某设备的评估历史记录
     * @param deviceId 设备ID
     * @param persona 用户画像
     * @return 历史记录列表
     */
    public List<AssessmentRecord> getHistory(String deviceId, UserPersona persona) {
        return repository.findByDeviceIdAndPersonaOrderByCreatedAtDesc(deviceId, persona);
    }

    /**
     * 获取某设备的所有评估记录
     * @param deviceId 设备ID
     * @return 所有记录列表
     */
    public List<AssessmentRecord> getAllHistory(String deviceId) {
        return repository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }
}