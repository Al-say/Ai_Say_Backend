package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import com.zhupinzan.speaking.model.dto.EvalAudioResp;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import com.zhupinzan.speaking.repository.DeviceRepository;
import com.zhupinzan.speaking.service.audio.AudioMetaService;
import com.zhupinzan.speaking.service.audio.AudioTranscodeService;
import com.zhupinzan.speaking.service.storage.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

/**
 * 评估编排服务 - 负责完整的音频评估流程
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvalOrchestratorService {

    private final DeviceRepository deviceRepo;
    private final LocalStorageService storage;
    private final AudioTranscodeService transcode;
    private final AudioMetaService meta;
    private final BaiduAsrService asr;
    private final DeepSeekEvalService deepSeek;
    private final AssessmentRecordRepository recordRepo;
    private final ProfileProgressService progressService;

    @Transactional
    public EvalAudioResp evaluateAudio(String deviceId, UserPersona persona, String scene, MultipartFile file) {
        log.info("🎯 开始音频评估流程 - 设备: {}, 场景: {}", deviceId, scene);

        // 1. 设备活跃更新 (外键兜底)
        deviceRepo.upsertTouch(deviceId);

        // 2. 音频预处理：转码为 16k Mono WAV
        byte[] raw;
        try {
            raw = file.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("文件读取失败");
        }

        String ext = getFileExtension(file.getOriginalFilename());
        var wav = transcode.transcodeToWav16kMono(raw, ext);

        // 3. 多媒体元数据与存储
        long durationMs = meta.durationMs(wav.bytes(), ".wav");
        var uploaded = storage.uploadAudio(wav.bytes(), deviceId, ".wav");

        // 4. ASR 语音转文字 (带容错)
        String transcript = "";
        try {
            transcript = asr.recognizeFromWavBytes(wav.bytes());
        } catch (Exception e) {
            log.error("ASR 识别失败: {}", e.getMessage());
            // MVP 阶段：ASR 失败可能导致评分不准，但我们继续流程
        }

        // 5. DeepSeek 智能评分 (核心逻辑)
        DeepSeekEvalResult evalResult;
        try {
            evalResult = deepSeek.evaluate(persona, scene, transcript);
        } catch (Exception e) {
            log.error("AI 评分失败: {}", e.getMessage());
            evalResult = createFallbackResult();
        }

        // 6. 结果持久化 (AssessmentRecord)
        AssessmentRecord record = new AssessmentRecord();
        record.setDeviceId(deviceId);
        record.setMode(AssessmentMode.AUDIO);
        record.setPersona(persona);
        record.setScene(scene);
        record.setAudioUrl(uploaded);
        record.setTranscript(transcript);

        // 映射评分指标
        record.setOverallScore(evalResult.overallScore() != null ? evalResult.overallScore().doubleValue() : 0.0);
        record.setFeedback(java.util.Map.of("summary", evalResult.feedback().summary()));

        recordRepo.save(record);

        // 7. 更新用户打卡进度
        progressService.onPracticeCompleted(deviceId, durationMs);

        // 8. 构造并返回响应
        return new EvalAudioResp(
                record.getId(),
                uploaded,
                durationMs,
                transcript,
                evalResult.overallScore(),
                evalResult.metrics().getOrDefault("fluency", evalResult.overallScore()), // fluency
                evalResult.metrics().getOrDefault("completeness", evalResult.overallScore()), // completeness
                evalResult.metrics().getOrDefault("relevance", evalResult.overallScore()), // relevance
                evalResult.feedback().summary() // 改为字符串
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null)
            return ".m4a";
        int lastIdx = filename.lastIndexOf(".");
        return lastIdx == -1 ? ".m4a" : filename.substring(lastIdx);
    }

    private DeepSeekEvalResult createFallbackResult() {
        return DeepSeekEvalResult.fallback("评分服务暂时不可用，请稍后重试");
    }
}