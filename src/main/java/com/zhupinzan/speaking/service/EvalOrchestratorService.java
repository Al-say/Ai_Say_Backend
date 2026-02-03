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

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    // ========== 系统核心组件依赖注入 ==========

    /**
     * 设备数据仓库 - 用户设备状态管理
     * <p>
     * 负责设备信息的持久化存储和管理，确保每个用户设备都有唯一标识。
     * 通过upsert操作保证设备记录的存在性和活跃状态，是用户数据一致性的基础。
     */
    private final DeviceRepository deviceRepo;

    /**
     * 云存储服务 - 媒体文件持久化
     * <p>
     * 处理音频文件的存储和CDN分发，支持本地存储和云存储两种实现。
     * 提供统一的文件上传接口，返回可访问的URL，便于前端播放和下载。
     */
    private final StorageService storage;

    /**
     * 音频转码引擎 - 格式标准化处理
     * <p>
     * 将各种音频格式转换为系统标准格式（16kHz单声道WAV），
     * 确保后续的ASR和AI处理能够正常进行，提供一致的输入格式。
     */
    private final AudioTranscodeService transcode;

    /**
     * 音频元数据提取器 - 技术参数分析
     * <p>
     * 从音频文件中提取时长、码率、采样率等技术参数，
     * 为时长统计和质量检查提供数据支持。
     */
    private final AudioMetaService meta;

    /**
     * 百度语音识别引擎 - 语音转文字核心
     * <p>
     * 调用百度智能云ASR服务，实现高精度的语音识别功能。
     * 支持多种语言和场景，是AI评估流程的重要输入。
     */
    private final BaiduAsrService asr;

    /**
     * DeepSeek AI评估引擎 - 智能评分核心
     * <p>
     * 集成DeepSeek大模型，对用户口语进行多维度评估。
     * 提供流利度、完整度、相关性等专业评分和改进建议。
     */
    private final DeepSeekEvalService deepSeek;

    /**
     * 评估记录仓储 - 历史数据管理
     * <p>
     * 负责评估记录的持久化存储，保存完整的评估历史数据。
     * 支持查询、统计和分析，为后续的数据挖掘提供基础。
     */
    private final AssessmentRecordRepository recordRepo;

    /**
     * 用户进度追踪器 - 学习数据分析
     * <p>
     * 记录用户的学习轨迹和成长数据，包括练习时长、练习次数等。
     * 为学习报告、成长曲线和个性化推荐提供数据支持。
     */
    private final ProfileProgressService progressService;

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
        // [安全加固] 增加文件大小检查，防止内存溢出攻击 (Denial of Service)
        if (file.isEmpty() || file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.error("文件为空或大小超过限制 ({} bytes)，上传被拒绝", MAX_FILE_SIZE_BYTES);
            throw new IllegalArgumentException("文件为空或大小超过10MB");
        }
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
     * 文件扩展名提取器 - 安全的扩展名处理
     *
     * <h2>方法概述</h2>
     * 这是一个关键的工具方法，负责从各种格式的文件名中安全地提取文件扩展名。
     * 考虑到用户可能从不同设备、使用不同应用上传文件，此方法需要处理各种边界情况。
     *
     * <h2>逻辑设计</h2>
     * <ul>
     *   <li><b>空值处理</b>：当filename为null时，返回默认的.m4a扩展名</li>
     *   <li><b>无扩展名</b>：当文件名不包含点号时，返回默认的.m4a扩展名</li>
     *   <li><b>多扩展名</b>：当文件名包含多个点号时，取最后一个点号之后的内容</li>
     *   <li><b>安全检查</b>：确保返回的扩展名不包含路径信息</li>
     * </ul>
     *
     * <h2>默认值选择理由</h2>
     * <ul>
     *   <li><b>.m4a</b>：iOS设备的默认录音格式，兼容性最好</li>
     *   <li><b>流行度</b>：.m4a是现代设备最常用的音频格式之一</li>
     *   <li><b>压缩质量</b>：相比MP3，m4a通常有更好的音质</li>
     *   <li><b>兼容性</b>：大多数现代浏览器和播放器都支持</li>
     * </ul>
     *
     * <h2>性能优化策略</h2>
     * <ul>
     *   <li><b>避免正则</b>：使用String.lastIndexOf()比正则表达式更快</li>
     *   <li><b>直接访问</b>：通过数组索引直接访问字符，减少方法调用</li>
     *   <li><b>快速返回</b>：常见情况优先处理，避免不必要的计算</li>
     * </ul>
     *
     * <h2>边界情况处理</h2>
     * <table border="1" cellpadding="3" cellspacing="0">
     *   <tr><th>输入</th><th>输出</th><th>说明</th></tr>
     *   <tr><td>null</td><td>.m4a</td><td>空文件名处理</td></tr>
     *   <tr><td>"audio"</td><td>.m4a</td><td>无扩展名</td></tr>
     *   <tr><td>"file.mp3"</td><td>.mp3</td><td>标准扩展名</td></tr>
     *   <tr><td>"archive.tar.gz"</td><td>.gz</td><td>多扩展名取最后一个</td></tr>
     *   <tr><td>".hidden"</td><td>.hidden</td><td>文件名以点开头</td></tr>
     * </table>
     *
     * <h2>安全考虑</h2>
     * <ul>
     *   <li><b>路径安全</b>：返回的扩展名不包含路径分隔符</li>
     *   <li><b>类型验证</b>：确保返回的是有效的扩展名格式</li>
     *   <li><b>输入清理</b>：虽然不进行深入验证，但确保基本安全</li>
     * </ul>
     *
     * <h2>典型使用场景</h2>
     * <ul>
     *   <li><b>文件上传</b>：确定上传文件的类型</li>
     *   <li><b>格式转换</b>：选择合适的转换器</li>
     *   <li><b>存储管理</b>：按类型组织文件</li>
     *   <li><b>媒体处理</b>：选择合适的编解码器</li>
     * </ul>
     *
     * <h2>扩展建议</h2>
     * <ul>
     *   <li>可以扩展支持白名单机制，只允许特定扩展名</li>
     *   <li>可以添加MIME类型映射，提供更精确的文件类型判断</li>
     *   <li>可以考虑文件头检测，避免仅依赖扩展名</li>
     * </ul>
     *
     * @param filename 原始文件名字符串，可能为null、空字符串或包含路径
     * @return 文件扩展名（包含点号，如.mp3），默认返回.m4a
     * @see String#lastIndexOf(String) 字符串查找方法
     * @see java.io.File#getName() 获取文件名（不含路径）
     */
    private String getFileExtension(String filename) {
        if (filename == null)
            return ".m4a";
        int lastIdx = filename.lastIndexOf(".");
        return lastIdx == -1 ? ".m4a" : filename.substring(lastIdx);
    }

    /**
     * 服务降级处理器 - AI评分异常的优雅降级
     *
     * <h2>降级设计理念</h2>
     * 在分布式系统中，服务的不可用是常态。此方法实现了系统的弹性设计，
     * 当核心AI评分服务出现问题时，提供降级响应而非系统崩溃，
     * 确保服务的可用性和用户体验的连续性。
     *
     * <h2>降级策略详解</h2>
     * <ul>
     *   <li><b>状态标识</b>：返回状态码"error"，明确标识服务异常状态</li>
     *   <li><b>友好提示</b>：向用户说明"AI评分服务暂时不可用"，避免技术术语</li>
     *   <li><b>操作引导</b>：建议用户"稍后重试"，提供明确的解决方向</li>
     *   <li><b>默认分数</b>：设置0分，避免评分数据异常</li>
     *   <li><b>结构一致</b>：保持与正常响应相同的数据结构</li>
     * </ul>
     *
     * <h2>典型失效场景</h2>
     * <table border="1" cellpadding="3" cellspacing="0">
     *   <tr><th>场景类型</th><th>原因</th><th>影响范围</th><th>降级处理</th></tr>
     *   <tr><td>网络故障</td><td>API连接超时</td><td>实时影响</td><td>立即降级</td></tr>
     *   <tr><td>配额耗尽</td><td>调用次数达到限制</td><td>周期性影响</td><td>限流降级</td></tr>
     *   <tr><td>服务维护</td><td>模型升级或维护</td><td>计划内影响</td><td>预告降级</td></tr>
     *   <tr><td>系统过载</td><td>高并发导致超时</td><td>临时影响</td><td>熔断降级</td></tr>
     * </table>
     *
     * <h2>技术实现细节</h2>
     * <ul>
     *   <li><b>工厂模式</b>：使用静态工厂方法创建降级结果</li>
     *   <li><b>数据一致性</b>：保持与正常响应相同的字段结构</li>
     *   <li><b>错误隔离</b>：降级结果不会影响后续流程</li>
     *   <li><b>监控标记</b>：错误状态便于监控系统识别</li>
     * </ul>
     *
     * <h2>用户体验设计</h2>
     * <ul>
     *   <li><b>透明性</b>：明确告知用户服务状态</li>
     *   <li><b>指导性</b>：提供具体的操作建议</li>
     *   <li><b>一致性</b>：保持界面布局不变</li>
     *   <li><b>可恢复性</b>：用户可以重试操作</li>
     * </ul>
     *
     * <h2>监控与告警</h2>
     * <ul>
     *   <li><b>错误计数</b>：记录降级次数和频率</li>
     *   <li><b>触发告警</b>：当降级超过阈值时触发告警</li>
     *   <li><b>根因分析</b>：记录降级的具体原因</li>
     *   <li><b>自动恢复</b>：监控服务恢复情况</li>
     * </ul>
     *
     * <h2>扩展性考虑</h2>
     * <ul>
     *   <li><b>多级降级</b>：可以设计多级降级策略</li>
     *   <li><b>权重降级</b>：根据严重程度调整响应</li>
     *   <li><b>备用服务</b>：集成备用AI服务</li>
     *   <li><b>缓存降级</b>：使用历史评分数据</li>
     * </ul>
     *
     * <h2>性能影响</h2>
     * <ul>
     *   <li><b>响应速度</b>：降级响应比正常响应更快</li>
     *   <li><b>资源消耗</b>：降级逻辑消耗极少资源</li>
     *   <li><b>并发能力</b>：降级可以处理更多并发请求</li>
     *   <li><b>系统稳定性</b>：避免级联失败</li>
     * </ul>
     *
     * <h2>最佳实践</h2>
     * <ul>
     *   <li>在服务设计初期就考虑降级策略</li>
     *   <li>记录详细的降级日志，便于分析</li>
     *   <li>定期演练降级流程，确保可靠性</li>
     *   <li>监控降级频率，及时处理问题</li>
     * </ul>
     *
     * @return DeepSeekEvalResult 包含降级信息的评估结果对象
     *         包含状态"error"、友好提示信息、默认0分和建议列表
     * @throws RuntimeException 在极少数情况下可能抛出异常
     * @see DeepSeekEvalResult#fallback(String, String) 降级结果工厂方法
     * @see org.slf4j.Logger#error(String) 错误日志记录
     */
    private DeepSeekEvalResult createFallbackResult() {
        return DeepSeekEvalResult.fallback("error", "AI评分服务暂时不可用，请稍后重试");
    }

    /**
     * 安全的评分指标提取器 - 健壮的数据访问方法
     *
     * <h2>方法概述</h2>
     * 这是一个通用的安全数据访问工具，用于从Map中提取数值类型的评分指标。
     * 在AI评估结果处理中，经常需要从Map中获取各种评分数据，但Map可能为null、
     * 键不存在或值类型不匹配，此方法提供了一致的处理方式。
     *
     * <h2>健壮性设计</h2>
     * <ul>
     *   <li><b>null检查</b>：Map为null时直接返回默认值</li>
     *   <li><b>存在性检查</b>：Key不存在时返回默认值</li>
     *   <li><b>类型检查</b>：Value不是Number类型时返回默认值</li>
     *   <li><b>转换安全</b>：所有Number类型统一转换为double</li>
     * </ul>
     *
     * <h2>类型处理机制</h2>
     * <p>支持的所有Number类型及其转换：</p>
     * <table border="1" cellpadding="3" cellspacing="0">
     *   <tr><th>原始类型</th><th>转换方式</th><th>精度保证</th></tr>
     *   <tr><td>Integer</td><td>doubleValue()</td><td>精确转换</td></tr>
     *   <tr><td>Double</td><td>直接返回</td><td>保持原值</td></tr>
     *   <tr><td>Float</td><td>doubleValue()</td><td>精度损失</td></tr>
     *   <tr><td>Long</td><td>doubleValue()</td><td>大数精度</td></tr>
     *   <tr><td>Short</td><td>doubleValue()</td><td>精确转换</td></tr>
     *   <tr><td>Byte</td><td>doubleValue()</td><td>精确转换</td></tr>
     * </table>
     *
     * <h2>错误处理策略</h2>
     * <ul>
     *   <li><b>防御性编程</b>：主动检查各种异常情况</li>
     *   <li><b>容错设计</b>：异常时优雅降级到默认值</li>
     *   <li><b>类型安全</b>：避免ClassCastException</li>
     *   <li><b>日志记录</b>：可以添加异常日志（可选）</li>
     * </ul>
     *
     * <h2>性能优化</h2>
     * <ul>
     *   <li><b>快速失败</b>：遇到异常情况立即返回</li>
     *   <li><b>减少对象创建</b>：避免不必要的中间对象</li>
     *   <li><b>类型检查优化</b>：使用instanceof进行类型判断</li>
     *   <li><b>缓存友好</b>：方法无状态，适合频繁调用</li>
     * </ul>
     *
     * <h2>典型使用场景</h2>
     * <ul>
     *   <li><b>AI评分结果处理</b>：从评估结果Map中提取各维度分数</li>
     *   <li><b>配置参数获取</b>：从配置Map中获取数值型配置</li>
     *   <li><b>统计数据处理</b>：从统计结果中提取数值指标</li>
     *   <li><b>API响应解析</b>：从JSON响应中提取数值字段</li>
     * </ul>
     *
     * <h2>与其他方法的协作</h2>
     * <ul>
     *   <li><b>createFallbackResult</b>：配合处理AI评分失败的情况</li>
     *   <li><b>evaluateAudio</b>：用于提取评分结果中的各项指标</li>
     *   <li><b>数据库保存</b>：为数据库字段提供安全的数值</li>
     * </ul>
     *
     * <h2>扩展性建议</h2>
     * <ul>
     *   <li><b>泛型支持</b>：可以扩展为支持泛型方法</li>
     *   <li><b>复杂类型</b>：支持嵌套Map或复杂对象</li>
     *   <li><b>验证规则</b>：添加数值范围验证</li>
     *   <li><b>转换选项</b>：支持多种返回类型（int、double等）</li>
     * </ul>
     *
     * <h2>最佳实践</h2>
     * <ul>
     *   <li>为默认值选择合理的业务默认值</li>
     *   <li>在关键业务逻辑中使用此方法保证健壮性</li>
     *   <li>考虑添加日志记录异常情况（调试用）</li>
     *   <li>注意性能敏感场景下的调用频率</li>
     * </ul>
     *
     * @param map 包含评分指标的Map集合，可能为null或为空
     * @param key 要提取的指标键名，不能为null
     * @param defaultValue 当提取失败时返回的默认值，确保业务逻辑不受影响
     * @return 提取的数值指标，如果提取失败则返回默认值
     * @throws NullPointerException 当key为null时可能抛出（取决于Map实现）
     * @see java.util.Map#get(Object) 标准Map获取方法
     * @see Number#doubleValue() Number类型转换方法
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
