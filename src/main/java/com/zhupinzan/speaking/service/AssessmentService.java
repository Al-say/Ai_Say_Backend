package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.AssessmentMode;
import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评估数据服务 - 评估记录管理与数据分析支持
 * <p>
 * 该服务专门负责评估数据的持久化管理和查询功能，为系统的Growth模块
 * 提供坚实的数据基础。它支持多种评估模式的记录存储，并提供了灵活的
 * 历史数据查询接口。
 * <p>
 * <b>核心功能：</b><br>
 * 1. 评估记录管理：支持文本和音频两种评估模式的记录保存<br>
 * 2. 智能模式推断：自动识别评估类型（文本/音频）<br>
 * 3. 数据转换：将DTO对象转换为实体对象并存储<br>
 * 4. 历史查询：提供多种维度的评估历史查询功能<br>
 * 5. 进度更新：同步更新用户的学习统计数据
 * <p>
 * <b>数据架构：</b><br>
 * • 扁平化字段：用于快速查询和索引（score、fluency等）<br>
 * • JSONB字段：存储复杂的结构化数据（feedback、metrics）<br>
 * • 关联字段：建立用户、设备、评估的关联关系<br>
 * • 时间戳：记录创建时间和更新时间
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

    /**
     * 评估记录数据仓库
     * <p>
     * 负责评估记录的CRUD操作，提供：
     * • 基础的保存和查询功能
     * • 复杂的条件查询（按设备、画像、时间等）
     * • 排序和分页支持
     * • 数据约束验证
     */
    private final AssessmentRecordRepository repository;

    /**
     * 用户进度服务
     * <p>
     * 在每次评估完成后，调用此服务更新用户的：
     * • 练习天数统计
     * • 累计练习时长
     * • 学习轨迹记录
     * 成就进度更新
     */
    private final ProfileProgressService profileProgressService;

    /**
     * 评估记录保存方法 - 核心数据持久化功能
     * <p>
     * 此方法实现了完整的评估记录保存流程，支持文本和音频两种评估模式。
     * 它能够智能推断评估类型，并合理地分配数据到不同的存储字段中。
     * <p>
     * <b>数据流转过程：</b><br>
     * 1. 实体对象创建：构建AssessmentRecord基础信息<br>
     * 2. 评估模式推断：根据音频URL自动判断评估类型<br>
     * 3. 分数数据映射：将DTO评分转换为实体字段<br>
     * 4. 反馈信息处理：结构化存储详细反馈<br>
     * 5. 数据持久化：保存到数据库<br>
     * 6. 前端数据回填：返回记录ID和时间戳<br>
     * 7. 进度更新：同步更新用户学习统计
     * <p>
     * <b>存储策略：</b><br>
     * • 扁平化存储：常用查询字段单独存储，提高查询效率<br>
     * • JSONB存储：复杂结构化数据存储，便于扩展<br>
     * • 智能推断：自动处理null和默认值<br>
     * • 数据转换：DTO到实体的类型转换
     *
     * @param deviceId 设备唯一标识，用于关联用户和设备
     * @param persona 用户画像类型，影响评估策略和数据分析
     * @param prompt 评估使用的提示词，用于记录评估上下文
     * @param userText 用户输入的文本内容，用于历史回溯
     * @param audioUrl 音频文件URL（可选），用于音频评估模式
     * @param result 评估结果数据传输对象，包含评分和反馈
     * @param mode 评估模式（可选），若为null则自动推断
     * @return AssessmentRecord 保存后的评估记录实体，包含数据库生成的ID
     */
    public AssessmentRecord saveAttempt(String deviceId, UserPersona persona, String prompt,
            String userText, String audioUrl, EvalDTO.TextEvalResp result, AssessmentMode mode) {

        // ====== 评估记录实体构建 ======

        // 创建评估记录基础实体
        AssessmentRecord record = new AssessmentRecord();
        record.setDeviceId(deviceId);
        record.setPersona(persona);
        record.setPrompt(prompt);
        record.setTranscript(userText); // 使用transcript字段存储用户文本
        record.setAudioUrl(audioUrl);

        // 【智能评估模式推断】
        // 如果未指定模式，根据是否有音频URL自动判断
        if (mode == null) {
            boolean hasAudio = audioUrl != null && !audioUrl.trim().isEmpty();
            mode = hasAudio ? AssessmentMode.AUDIO : AssessmentMode.TEXT;
            log.debug("自动推断评估模式: {}", mode);
        }
        record.setMode(mode);

        // 设置默认评估场景，未来可从Controller传入
        record.setScene("practice");

        // ====== 分数数据映射 ======

        // 将评估结果映射到实体的扁平化字段中，便于快速查询
        record.setFluency(result.getFluency());
        record.setCompleteness(result.getCompleteness());
        record.setRelevance(result.getRelevance());
        record.setOverallScore(result.getOverallScore());

        // ====== 反馈信息结构化存储 ======

        // 初始化反馈JSON字段
        if (record.getFeedback() == null) {
            record.setFeedback(new java.util.HashMap<>());
        }

        // 【问题信息处理】
        // 将评估中发现的问题转换为结构化数据存储
        if (result.getIssues() != null && !result.getIssues().isEmpty()) {
            List<Map<String, Object>> issuesMaps = result.getIssues().stream()
                    .map(issue -> Map.of(
                            "offset", issue.getOffset(),        // 问题在文本中的位置
                            "length", issue.getLength(),        // 问题的文本长度
                            "message", issue.getMessage(),      // 问题描述
                            "replacements", issue.getReplacements())) // 建议的修正方案
                    .collect(java.util.stream.Collectors.toList());
            record.getFeedback().put("issues", issuesMaps);
            log.debug("保存问题信息: {}条", issuesMaps.size());
        }

        // 【建议信息处理】
        // 保存改进建议，用于指导用户学习
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            record.getFeedback().put("suggestions", result.getSuggestions());
            log.debug("保存建议信息: {}条", result.getSuggestions().size());
        }

        // ====== 数据持久化 ======

        // 保存评估记录到数据库
        AssessmentRecord saved = repository.save(record);
        log.info("评估记录保存成功，ID: {}, 设备: {}, 模式: {}",
                saved.getId(), deviceId, mode);

        // ====== 前端数据回填 ======

        // 将数据库生成的ID和创建时间回填到结果对象中
        result.setRecordId(saved.getId());
        result.setCreatedAt(saved.getCreatedAt().toString());

        // ====== 用户进度更新 ======

        // 更新用户的学习进度
        // 注意：当前使用固定时长（1分钟），未来可从音频元数据获取实际时长
        long durationMs = 60000L; // 1分钟 = 60000毫秒
        profileProgressService.onPracticeCompleted(deviceId, durationMs);
        log.debug("用户进度已更新，练习时长: {}ms", durationMs);

        return saved;
    }

    // ====== 历史记录查询方法 ======

    /**
     * 按设备和画像查询评估历史 - 精确的历史记录检索
     * <p>
     * 此方法提供精确的评估历史查询，根据设备ID和用户画像进行筛选。
     * 返回的结果按创建时间倒序排列，确保最新的记录排在前面。
     * <p>
     * <b>查询特点：</b><br>
     * • 双重过滤：同时匹配设备ID和用户画像<br>
     * • 时间排序：按创建时间降序排列<br>
     * • 完整记录：返回AssessmentRecord完整实体<br>
     * • 性能优化：利用数据库索引快速查询
     * <p>
     * <b>使用场景：</b><br>
     • 用户查看特定画像的学习历史<br>
     • 分析特定用户群体的学习数据<br>
     * 生成个性化的学习报告<br>
     * 学习轨迹可视化
     *
     * @param deviceId 设备唯一标识，用于精确匹配用户设备
     * @param persona 用户画像类型，用于筛选特定类型的评估记录
     * @return List<AssessmentRecord> 按时间倒序排列的评估记录列表
     */
    public List<AssessmentRecord> getHistory(String deviceId, UserPersona persona) {
        log.debug("查询评估历史 - 设备: {}, 画像: {}", deviceId, persona);

        List<AssessmentRecord> records = repository.findByDeviceIdAndPersonaOrderByCreatedAtDesc(deviceId, persona);

        log.info("查询到 {} 条评估记录", records.size());
        return records;
    }

    /**
     * 获取设备所有评估记录 - 全面的历史数据查询
     * <p>
     * 此方法获取指定设备ID的所有评估记录，无论用户画像如何。
     * 它提供了设备级别的完整学习历史，适用于需要查看用户全部
     * 学习活动的场景。
     * <p>
     * <b>查询范围：</b><br>
     * • 全量查询：不限制用户画像类型<br>
     * • 设备级视图：展示该设备上的所有评估活动<br>
     * • 时间序列：按时间倒序排列，便于查看最新活动<br>
     * • 数据完整性：包含所有评估模式的记录
     * <p>
     * <b>应用场景：</b><br>
     • 用户的学习总览和统计<br>
     * 学习习惯分析<br>
     * 设备使用情况监控<br>
     * 学习趋势分析
     *
     * @param deviceId 设备唯一标识，用于查询该设备上的所有评估记录
     * @return List<AssessmentRecord> 包含所有评估模式的记录列表，按时间倒序排列
     */
    public List<AssessmentRecord> getAllHistory(String deviceId) {
        log.debug("查询设备所有评估历史 - 设备: {}", deviceId);

        List<AssessmentRecord> records = repository.findByDeviceIdOrderByCreatedAtDesc(deviceId);

        log.info("查询到设备 {} 的全部评估记录: {} 条", deviceId, records.size());

        // 统计各评估模式的数量
        long audioCount = records.stream()
                .filter(r -> r.getMode() == AssessmentMode.AUDIO)
                .count();
        long textCount = records.stream()
                .filter(r -> r.getMode() == AssessmentMode.TEXT)
                .count();

        log.debug("评估模式分布 - 音频: {}, 文本: {}", audioCount, textCount);

        return records;
    }
}