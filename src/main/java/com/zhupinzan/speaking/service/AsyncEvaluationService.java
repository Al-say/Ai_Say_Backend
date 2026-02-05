package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.repository.EvaluationTaskRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步评估任务管理服务
 * 管理长时间运行的 AI 评估任务，避免 HTTP 超时。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEvaluationService {

    private final DeepSeekEvalService deepSeekEvalService;
    private final EvaluationTaskRepository evaluationTaskRepository;

    /**
     * 提交异步评估任务
     */
    public AsyncEvaluationResponse submitEvaluation(UserPersona persona, String scene,
                                                    String transcript, String userIdentity) {
        String taskId = UUID.randomUUID().toString();
        log.info("🔐 创建异步评估任务: taskId={}, owner={}, persona={}, scene={}",
                taskId, userIdentity, persona, scene);

        EvaluationTask task = EvaluationTask.builder()
                .id(taskId)
                .userIdentity(userIdentity)
                .persona(persona)
                .scene(scene)
                .transcript(transcript)
                .status(AsyncEvaluationResponse.TaskStatus.PENDING)
                .progress(0)
                .build();
        evaluationTaskRepository.save(task);

        executeEvaluationAsync(taskId, persona, scene, transcript);

        return AsyncEvaluationResponse.pending(taskId);
    }

    /**
     * 异步执行 AI 评估
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> executeEvaluationAsync(String taskId, UserPersona persona,
                                                          String scene, String transcript) {
        try {
            updateTaskStatus(taskId, AsyncEvaluationResponse.processing(taskId, 20));
            DeepSeekEvalResult result = deepSeekEvalService.evaluate(persona, scene, transcript);
            updateTaskStatus(taskId, AsyncEvaluationResponse.completed(taskId, result));
        } catch (Exception e) {
            log.error("异步评估失败: taskId={}, error={}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, AsyncEvaluationResponse.failed(taskId, e.getMessage()));
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 查询任务状态（校验所有权）
     */
    public AsyncEvaluationResponse getTaskStatus(String taskId, String userIdentity) {
        return evaluationTaskRepository.findById(taskId)
                .map(task -> {
                    if (!task.getUserIdentity().equals(userIdentity)) {
                        log.warn("🚨 访问拒绝: taskId={}, owner={}, requestedBy={}", taskId, task.getUserIdentity(), userIdentity);
                        return AsyncEvaluationResponse.failed(taskId, "无权访问此任务");
                    }
                    return AsyncEvaluationResponse.builder()
                            .taskId(task.getId())
                            .status(task.getStatus())
                            .progress(task.getProgress())
                            .result(task.getResult())
                            .errorMessage(task.getErrorMessage())
                            .createdAt(task.getCreatedAt() != null
                                    ? LocalDateTime.ofInstant(task.getCreatedAt(), ZoneOffset.UTC)
                                    : null)
                            .completedAt(task.getCompletedAt())
                            .build();
                })
                .orElseGet(() -> AsyncEvaluationResponse.failed(taskId, "任务不存在或已过期"));
    }

    /**
     * 删除任务（清理资源）
     */
    public boolean deleteTask(String taskId, String userIdentity) {
        return evaluationTaskRepository.findById(taskId)
                .map(task -> {
                    if (!task.getUserIdentity().equals(userIdentity)) {
                        log.warn("🚨 删除拒绝: taskId={}, owner={}, requestedBy={}", taskId, task.getUserIdentity(), userIdentity);
                        return false;
                    }
                    evaluationTaskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }

    public List<EvaluationTask> getUserHistory(String userIdentity) {
        return evaluationTaskRepository.findByUserIdentityOrderByCreatedAtDesc(userIdentity);
    }

    private void updateTaskStatus(String taskId, AsyncEvaluationResponse response) {
        evaluationTaskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(response.getStatus());
            task.setProgress(response.getProgress());
            task.setErrorMessage(response.getErrorMessage());
            task.setResult(response.getResult());
            task.setCompletedAt(response.getCompletedAt());
            evaluationTaskRepository.save(task);
        });
    }
}
