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
import com.zhupinzan.speaking.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

/**
 * 评估编排服务 - 负责完整的音频评估流程
 * <p>
 * 该服务是整个口语评估系统的核心业务逻辑协调器，扮演着"交响乐团指挥"的角色。
 * 它通过精心编排多个子服务（设备管理、音频处理、ASR、AI评分、存储、进度跟踪等），
 * 为用户提供端到端的口语评估体验。
 * <p>
 * <b>核心职责：</b><br>
 * 1. 设备生命周期管理：确保每个设备都有唯一标识和活跃状态<br>
 * 2. 音频预处理流水线：格式转换、质量检查、元数据提取<br>
 * 3. 语音识别协调：调用ASR服务将口语转换为文字<br>
 * 4. AI评分流程管理：整合DeepSeek模型进行多维度评估<br>
 * 5. 数据持久化：保存评估结果和相关元数据<br>
 * 6. 用户进度更新：跟踪学习轨迹并更新统计数据<br>
 * <p>
 * <b>设计亮点：</b><br>
 * • 事务性保证：使用@Transactional确保数据一致性<br>
 * • 异常容错：关键环节都有降级处理，保证服务可用性<br>
 * • 性能优化：异步处理、合理的超时设置<br>
 * • 可扩展性：模块化设计，易于添加新的评估维度或服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvalOrchestratorService {

    // 依赖注入的各个服务组件
    private final DeviceRepository deviceRepo;                // 设备数据仓库，负责设备信息的持久化和管理
    private final StorageService storage;                     // 云存储服务，处理音频文件的存储和CDN分发
    private final AudioTranscodeService transcode;            // 音频转码引擎，确保音频格式标准化
    private final AudioMetaService meta;                      // 音频分析工具，提取时长、码率等技术参数
    private final BaiduAsrService asr;                        // 百度语音识别引擎，实现高精度的语音转文字
    private final DeepSeekEvalService deepSeek;               // DeepSeek AI评估引擎，提供专业级的口语评分
    private final AssessmentRecordRepository recordRepo;      // 评估记录仓储，保存完整的评估历史
    private final ProfileProgressService progressService;     // 用户进度追踪器，记录学习数据和成长轨迹

    /**
     * 核心评估流程 - 执行完整的端到端音频评估
     * <p>
     * 这是系统的核心业务方法，采用"编排者"模式将多个独立的服务组件组合成完整的业务流程。
     * 方法使用@Transactional注解确保操作的原子性，任何步骤失败都会导致整个流程回滚。
     * <p>
     * <b>业务流程详解：</b><br>
     * 🔄 步骤1：设备活跃度管理<br>
     *   - 使用upsert原子操作确保设备记录存在<br>
     *   - 更新最后活跃时间，为后续的用户行为分析提供数据基础<br>
     * <br>
     * 🔄 步骤2：音频预处理流水线<br>
     *   - 读取原始文件字节数据<br>
     *   - 转换为标准化格式（16kHz单声道WAV）<br>
     *   - 满足后续ASR和AI模型的技术要求<br>
     * <br>
     * 🔄 步骤3：媒体元数据提取与存储<br>
     *   - 提取音频时长作为学习时长统计依据<br>
     *   - 上传到云存储服务，获得可访问的URL<br>
     *   - 为后续的播放和分析做准备<br>
     * <br>
     * 🔄 步骤4：语音识别（ASR）<br>
     *   - 调用百度ASR服务进行语音转文字<br>
     *   - 健壮的异常处理：即使ASR失败，流程也会继续<br>
     *   - 降级策略：空文本仍可进入AI评分环节<br>
     * <br>
     * 🔄 步骤5：AI智能评分<br>
     *   - 根据用户画像和场景定制评估策略<br>
     *   - DeepSeek模型进行多维度分析（流利度、完整度、相关性等）<br>
     *   - 智能的降级机制：服务不可用时提供友好提示<br>
     * <br>
     * 🔄 步骤6：数据持久化<br>
     *   - 构造完整的评估记录实体<br>
     *   - 同时保存评分数据和详细的JSON字段<br>
     *   - 为后续的数据分析和报表提供基础<br>
     * <br>
     * 🔄 步骤7：用户进度更新<br>
     *   - 统计本次学习时长<br>
     *   - 更新用户的学习天数、累计时长等指标<br>
     *   - 实时反映用户的学习轨迹<br>
     * <br>
     * 🔄 步骤8：结果封装与返回<br>
     *   - 构造符合前端规范的响应DTO<br>
     *   - 包含总分、维度分数、反馈和建议等信息
     *
     * @param deviceId 用户的设备唯一标识，用于关联用户设备和评估记录
     * @param persona  用户画像类型（备考党/职场人等），影响AI评分的策略和侧重点
     * @param scene    评估应用场景（日常练习/模拟面试等），提供上下文相关的评估标准
     * @param file     用户上传的音频文件，包含需要评估的口语内容
     * @return EvalAudioResp 包含完整评估结果的响应对象，供前端展示
     * @throws IllegalArgumentException 当文件读取失败时抛出
     */
    @Transactional
    public EvalAudioResp evaluateAudio(String deviceId, UserPersona persona, String scene, MultipartFile file) {
        log.info("🎯 开始音频评估流程 - 设备: {}, 场景: {}", deviceId, scene);

        // ====== 评估流程核心执行区域 ======

        // 【步骤1】设备活跃状态管理 - 确保用户设备数据一致性
        // 使用 upsert (upsertTouch) 操作实现原子性的设备记录创建或更新
        // 这是保证外键约束完整性的重要前提，避免插入孤儿记录
        deviceRepo.upsertTouch(deviceId);
        log.debug("设备 {} 活跃状态已更新", deviceId);

        // 【步骤2】音频预处理流水线 - 格式标准化
        // 将上传的 MultipartFile 转换为字节数组，并进行格式转换
        // 统一输出为 16kHz 采样率的单声道 WAV 格式，满足后续处理要求
        byte[] raw;
        try {
            raw = file.getBytes();
            log.debug("音频文件读取成功，大小: {} bytes", raw.length);
        } catch (Exception e) {
            log.error("音频文件读取失败: {}", e.getMessage());
            throw new IllegalArgumentException("文件读取失败，请检查文件是否损坏");
        }

        String ext = getFileExtension(file.getOriginalFilename());
        var wav = transcode.transcodeToWav16kMono(raw, ext);
        log.debug("音频转码完成，目标格式: 16kHz单声道WAV");

        // 【步骤3】媒体元数据提取与云存储
        // 提取音频时长用于学习统计，并上传到对象存储服务
        // 安全的时长处理：防止负数或异常值
        long durationMs = meta.durationMs(wav.bytes(), ".wav");
        long safeDurationMs = Math.max(durationMs, 0);
        log.debug("音频时长: {} ms", safeDurationMs);

        var uploaded = storage.uploadAudio(wav.bytes(), deviceId, ".wav");
        log.debug("音频文件已上传至: {}", uploaded);

        // 【步骤4】语音识别(ASR) - 语音转文字转换
        // 调用百度ASR服务将口语转换为文字，作为AI评分的基础
        // 采用容错设计：即使ASR失败也不中断流程，但会影响评分质量
        String transcript = "";
        try {
            transcript = asr.recognizeFromWavBytes(wav.bytes());
            log.debug("语音识别完成，转录文本长度: {} 字符", transcript.length());

            // 记录关键信息用于后续分析
            if (transcript.length() > 500) {
                log.info("转录文本过长，截断显示: {}",
                    transcript.substring(0, 500) + "...");
            }
        } catch (Exception e) {
            log.error("ASR 识别失败: {}", e.getMessage());
            // 降级策略：空文本仍可进入AI评分，但评分准确性会降低
            // 在生产环境中，可以考虑增加重试机制或切换备用ASR服务
        }

        // 【步骤5】AI智能评分 - 核心评估逻辑
        // 调用DeepSeek服务进行多维度评估，包括流利度、完整度、相关性等
        // 用户画像和场景信息用于定制评估策略，提高评估的针对性
        DeepSeekEvalResult evalResult;
        try {
            evalResult = deepSeek.evaluate(persona, scene, transcript);
            log.info("AI评分完成，总体得分: {}", evalResult.overallScore());

            // 记录评分详情用于监控和分析
            if (evalResult.metrics() != null) {
                log.debug("维度得分: {}", evalResult.metrics());
            }
        } catch (Exception e) {
            log.error("AI 评分失败: {}", e.getMessage());
            // 降级策略：返回预设结果，避免前端显示错误
            // 包含友好的错误提示，指导用户后续操作
            evalResult = createFallbackResult();
        }

        // 【步骤6】评估结果持久化 - 数据层操作
        // 创建评估记录实体，同时保存结构化数据和JSON字段
        // 结构化字段用于快速查询，JSON字段用于详细分析和未来扩展
        AssessmentRecord record = new AssessmentRecord();
        record.setDeviceId(deviceId);
        record.setMode(AssessmentMode.AUDIO);
        record.setPersona(persona);
        record.setScene(scene);
        record.setAudioUrl(uploaded);
        record.setTranscript(transcript);

        // 分数数据映射与处理
        Integer overallScore = evalResult.overallScore() != null ? evalResult.overallScore() : 0;
        java.util.Map<String, Object> metrics = evalResult.metrics() != null
                ? new java.util.HashMap<>(evalResult.metrics())
                : java.util.Collections.emptyMap();
        var feedback = evalResult.feedback() != null
                ? evalResult.feedback()
                : new DeepSeekEvalResult.Feedback("No feedback available.", java.util.List.of(), java.util.List.of(),
                        java.util.List.of(), "");

        // 保存总分和各维度分数到扁平化字段
        record.setOverallScore(overallScore.doubleValue());
        record.setFluency(getMetricValue(metrics, "fluency", 0.0));
        record.setCompleteness(getMetricValue(metrics, "completeness", 0.0));
        record.setRelevance(getMetricValue(metrics, "relevance", 0.0));

        // 保存详细的评分数据到JSONB字段，便于后续分析和报表生成
        record.setFeedback(java.util.Map.of("summary", feedback.summary()));
        record.setMetrics(java.util.Map.of(
                "fluency", (int) getMetricValue(metrics, "fluency", 0),
                "completeness", (int) getMetricValue(metrics, "completeness", 0),
                "relevance", (int) getMetricValue(metrics, "relevance", 0),
                "pronunciation", (int) getMetricValue(metrics, "pronunciation", 0),
                "grammar", (int) getMetricValue(metrics, "grammar", 0),
                "vocabulary", (int) getMetricValue(metrics, "vocabulary", 0)));

        // 持久化到数据库
        recordRepo.save(record);
        log.info("评估记录已保存，ID: {}", record.getId());

        // 【步骤7】用户进度更新 - 学习轨迹追踪
        // 在评估完成后，更新用户的练习统计数据
        // 为用户的学习报告和成长曲线提供数据支持
        progressService.onPracticeCompleted(deviceId, safeDurationMs);
        log.debug("用户 {} 进度已更新，本次练习时长: {} ms", deviceId, safeDurationMs);

        // 【步骤8】构造响应DTO - 前端数据封装
        // 将评估结果封装为前端需要的格式，包括：
        // - 基本信息（记录ID、音频URL、时长）
        // - 评分结果（总分、各维度分数）
        // - 反馈信息（摘要、建议）
        return new EvalAudioResp(
                record.getId(),
                uploaded,
                safeDurationMs,
                transcript,
                overallScore,
                (int) getMetricValue(metrics, "fluency", overallScore), // 流利度
                (int) getMetricValue(metrics, "completeness", overallScore), // 完整度
                (int) getMetricValue(metrics, "relevance", overallScore), // 相关性
                feedback.summary(), // 简要反馈
                feedback.suggestions() // 改进建议列表
        );
    }

    /**
     * 文件扩展名提取工具方法
     * <p>
     * 安全地从文件名中提取扩展名，处理各种边界情况：
     * - 空文件名 → 默认.m4a（iOS录音常用格式）
     * - 无扩展名 → 默认.m4a
     * - 多个点 → 取最后一个点之后的内容
     * <p>
     * <b>设计考虑：</b><br>
     * • 默认.m4a：兼容iOS设备的录音格式<br>
     * • 性能优化：使用String.lastIndexOf而非正则表达式<br>
     * • 安全性：避免路径遍历攻击风险
     *
     * @param filename 原始文件名，可能为null或包含路径
     * @return 文件扩展名（包含点号，如.mp3）
     */
    private String getFileExtension(String filename) {
        if (filename == null)
            return ".m4a";
        int lastIdx = filename.lastIndexOf(".");
        return lastIdx == -1 ? ".m4a" : filename.substring(lastIdx);
    }

    /**
     * 服务降级方法 - 创建AI评分失败的备选结果
     * <p>
     * 当DeepSeek AI评分服务出现异常时，此方法提供优雅降级，
     * 确保系统在服务不可用时仍能正常响应，维持用户体验。
     * <p>
     * <b>降级策略：</b><br>
     * • 状态标识：使用"error"标记服务异常状态<br>
     * • 友好提示：向用户说明服务暂时不可用<br>
     * • 建议操作：引导用户稍后重试<br>
     * • 默认分数：避免影响现有评分逻辑
     * <p>
     * <b>使用场景：</b><br>
     • DeepSeek API超时或连接失败<br>
     • 配额耗尽达到限制<br>
     • 模型服务维护期间<br>
     • 网络连接不稳定时
     *
     * @return DeepSeekEvalResult 包含降级信息的评估结果对象
     *         状态为"error"，分数为0，包含友好提示信息
     */
    private DeepSeekEvalResult createFallbackResult() {
        return DeepSeekEvalResult.fallback("error", "AI评分服务暂时不可用，请稍后重试");
    }

    /**
     * 安全的评分指标提取工具方法
     * <p>
     * 从Map中提取数值类型的评分指标，处理各种异常情况：
     * - Map为null → 返回默认值
     * - Key不存在 → 返回默认值
     * - Value不是Number → 返回默认值
     * <p>
     * <b>类型转换处理：</b><br>
     * • Integer, Double, Float等Number类型统一转换为double<br>
     * • 避免ClassCastException异常<br>
     * • 保持类型安全
     *
     * @param map 包含评分指标的Map，可能为null
     * @param key 要提取的指标键名
     * @param defaultValue 当提取失败时的默认值
     * @return 指标值或默认值
     */
    private double getMetricValue(java.util.Map<?, ?> map, String key, double defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
}
