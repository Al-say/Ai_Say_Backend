package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异步评估任务响应 DTO
 * <p>
 * <b>设计目的：</b>解决 AI 评估的长延迟问题，避免 HTTP 超时。
 * <p>
 * <b>工作流程：</b><br>
 * 1. 客户端提交评估请求 → 立即返回 TaskID<br>
 * 2. 客户端使用 TaskID 轮询状态<br>
 * 3. 任务完成后，返回完整的评估结果
 * <p>
 * <b>状态机：</b><br>
 * PENDING → PROCESSING → COMPLETED / FAILED
 * <p>
 * <b>使用场景：</b><br>
 * • 长时间的 AI 评估（5-30秒）<br>
 * • 异步任务追踪<br>
 * • 避免 HTTP 请求超时<br>
 * • 支持重试和恢复机制
 *
 * @author system
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncEvaluationResponse {

    /**
     * 任务唯一标识符
     * <p>使用 UUID 确保唯一性，客户端用于轮询任务状态</p>
     */
    private String taskId;

    /**
     * 任务状态
     * <p>
     * • PENDING: 任务已提交，等待处理<br>
     * • PROCESSING: 正在进行 AI 评估<br>
     * • COMPLETED: 评估成功完成<br>
     * • FAILED: 评估失败（超时、错误等）
     * </p>
     */
    private TaskStatus status;

    /**
     * 任务进度百分比 (0-100)
     * <p>可选字段，用于显示处理进度</p>
     */
    private Integer progress;

    /**
     * 完整的评估结果
     * <p>仅在 status = COMPLETED 时返回</p>
     */
    private DeepSeekEvalResult result;

    /**
     * 错误信息
     * <p>仅在 status = FAILED 时返回</p>
     */
    private String errorMessage;

    /**
     * 任务创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 任务完成时间
     * <p>仅在任务完成或失败时设置</p>
     */
    private LocalDateTime completedAt;

    /**
     * 预估剩余时间（秒）
     * <p>基于历史数据的估算</p>
     */
    private Integer estimatedSecondsRemaining;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        /** 任务已提交，等待处理 */
        PENDING,
        /** 正在处理中 */
        PROCESSING,
        /** 处理成功完成 */
        COMPLETED,
        /** 处理失败 */
        FAILED
    }

    /**
     * 创建一个 PENDING 状态的任务响应
     */
    public static AsyncEvaluationResponse pending(String taskId) {
        return AsyncEvaluationResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.PENDING)
                .progress(0)
                .createdAt(LocalDateTime.now())
                .estimatedSecondsRemaining(15)
                .build();
    }

    /**
     * 创建一个 PROCESSING 状态的任务响应
     */
    public static AsyncEvaluationResponse processing(String taskId, int progress) {
        return AsyncEvaluationResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.PROCESSING)
                .progress(progress)
                .estimatedSecondsRemaining(Math.max(0, 15 - (progress * 15 / 100)))
                .build();
    }

    /**
     * 创建一个 COMPLETED 状态的任务响应
     */
    public static AsyncEvaluationResponse completed(String taskId, DeepSeekEvalResult result) {
        return AsyncEvaluationResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.COMPLETED)
                .progress(100)
                .result(result)
                .completedAt(LocalDateTime.now())
                .estimatedSecondsRemaining(0)
                .build();
    }

    /**
     * 创建一个 FAILED 状态的任务响应
     */
    public static AsyncEvaluationResponse failed(String taskId, String errorMessage) {
        return AsyncEvaluationResponse.builder()
                .taskId(taskId)
                .status(TaskStatus.FAILED)
                .errorMessage(errorMessage)
                .completedAt(LocalDateTime.now())
                .build();
    }
}
