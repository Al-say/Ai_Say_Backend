package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 口语评估请求 DTO
 * <p>
 * <b>数据契约：</b>定义客户端（iOS）提交评估任务时的标准格式。
 * <p>
 * <b>设计原则：</b><br>
 * • 强类型验证：使用 Bean Validation 确保数据完整性<br>
 * • 清晰语义：每个字段都有明确的业务含义<br>
 * • 扩展性：预留字段支持未来功能
 * <p>
 * <b>工作流程：</b><br>
 * 1. iOS 客户端录音 → 上传音频文件<br>
 * 2. 后端 ASR 转写 → 得到 transcript<br>
 * 3. 构建此 DTO → 调用 DeepSeek 评估<br>
 * 4. 返回结构化结果
 * <p>
 * <b>关键字段说明：</b><br>
 * • <b>persona</b>: 用户画像（EXAM_PREP/CAREER_GROWTH），决定评估侧重点<br>
 * • <b>scene</b>: 评估场景（面试/日常练习），提供上下文信息<br>
 * • <b>transcript</b>: ASR 转写的文本，AI 评估的直接依据<br>
 * • <b>audioUrl</b>: 音频文件的存储路径（可选，用于二次分析）
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluationRequest {

    /**
     * 用户画像类型
     * <p>
     * 可选值：<br>
     * • EXAM_PREP: 备考党（侧重学术表达、复杂语法）<br>
     * • CAREER_GROWTH: 职场人（侧重商务沟通、简洁表达）<br>
     * • GENERAL: 通用学习者（平衡各维度）
     * </p>
     */
    @NotBlank(message = "用户画像类型不能为空")
    private String persona;

    /**
     * 评估场景
     * <p>
     * 示例：<br>
     * • "job_interview": 模拟面试<br>
     * • "daily_practice": 日常练习<br>
     * • "exam_simulation": 考试模拟<br>
     * • "business_presentation": 商务演讲
     * </p>
     */
    @NotBlank(message = "评估场景不能为空")
    private String scene;

    /**
     * ASR 转写的英文文本
     * <p>
     * 这是 AI 评估的核心输入。<br>
     * <b>质量要求：</b><br>
     * • 至少 8 个单词（过短无法评估）<br>
     * • 必须是英文（非英文将被拒绝）<br>
     * • 准确的转写（ASR 错误会影响评分）
     * </p>
     */
    @NotBlank(message = "转写文本不能为空")
    private String transcript;

    /**
     * 音频文件的 URL（可选）
     * <p>
     * 用于：<br>
     * • 后续的发音分析（需要专门的语音识别 API）<br>
     * • 数据审计和复查<br>
     * • 训练自定义模型
     * </p>
     */
    private String audioUrl;

    /**
     * 用户 ID（可选）
     * <p>用于关联用户历史记录和个性化推荐</p>
     */
    private String userId;

    /**
     * 任务优先级（可选）
     * <p>
     * • HIGH: 高优先级（付费用户）<br>
     * • NORMAL: 普通优先级<br>
     * • LOW: 低优先级
     * </p>
     */
    private String priority;

    /**
     * 是否异步处理（可选）
     * <p>
     * • true: 立即返回 TaskID，客户端轮询结果<br>
     * • false: 同步等待 AI 评估完成（可能超时）
     * </p>
     */
    @Builder.Default
    private Boolean async = true;

    /**
     * 客户端版本号（可选）
     * <p>用于兼容性判断和问题追踪</p>
     */
    private String clientVersion;
}
