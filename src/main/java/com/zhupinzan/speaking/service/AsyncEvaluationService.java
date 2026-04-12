package com.zhupinzan.speaking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhupinzan.speaking.model.dto.EvaluationRequest;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.model.TaskStatus;
import com.zhupinzan.speaking.repository.EvaluationTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEvaluationService {

    private final DeepSeekEvalService deepSeekService;
    private final EvaluationTaskRepository taskRepository; // 💉 注入仓库
    private final ObjectMapper objectMapper; // 用于序列化 JSON

    @Async("asyncTaskExecutor") // 使用我们配置的线程池
    public void processEvaluation(String taskId, EvaluationRequest request) {
        // 1. 再次查询任务（确保它是最新的）
        EvaluationTask task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            log.error("Task not found for processing: {}", taskId);
            return;
        }

        try {
            // 2. 调用 AI
            var persona = request.getPersona();
            var scene = request.getScene();
            var transcript = request.getTranscript();
            DeepSeekEvalResult assessmentResult;
            if (persona == null || persona.isBlank() || scene == null || scene.isBlank()) {
                assessmentResult = deepSeekService.evaluate(transcript);
            } else {
                assessmentResult = deepSeekService.evaluate(
                        com.zhupinzan.speaking.model.UserPersona.valueOf(persona.toUpperCase()),
                        scene,
                        transcript
                );
            }
            
            task.setResult(objectMapper.writeValueAsString(assessmentResult));
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(java.time.OffsetDateTime.now());
            
        } catch (Exception e) {
            log.error("Task failed: {}", taskId, e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        } finally {
            // 3. 💾 最终状态落库
            taskRepository.save(task);
        }
    }

    // 提交任务
    public AsyncEvaluationResponse submitEvaluation(com.zhupinzan.speaking.model.UserPersona persona, String scene, String transcript, String userEmail) {
        String taskId = UUID.randomUUID().toString();

        // 1. 先在数据库占个位
        EvaluationTask task = new EvaluationTask();
        task.setId(taskId);
        task.setUserIdentity(userEmail);
        task.setPersona(persona != null ? persona.name() : null);
        task.setScene(scene);
        task.setTranscript(transcript);
        task.setStatus(TaskStatus.PENDING);
        task.setProgress(0);
        taskRepository.save(task);

        // 2. 触发异步处理
        EvaluationRequest req = new EvaluationRequest();
        req.setTranscript(transcript);
        req.setPersona(persona == null ? null : persona.name());
        req.setScene(scene);
        processEvaluation(taskId, req);

        return AsyncEvaluationResponse.pending(taskId);
    }

    // 获取单个任务详情
    public EvaluationTask getTaskResult(String taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    /**
     * 获取任务状态并进行权限校验
     */
    public AsyncEvaluationResponse getTaskStatus(String taskId, String userEmail) {
        EvaluationTask task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }
        if (task.getUserIdentity() != null && userEmail != null && !task.getUserIdentity().equals(userEmail)) {
            throw new SecurityException("Access denied: You are not the owner of this task");
        }
        return mapToResponse(task);
    }

    // 获取用户历史
    public List<EvaluationTask> getUserHistory(String userIdentity) {
        return taskRepository.findByUserIdentityOrderByCreatedAtDesc(userIdentity);
    }

    private AsyncEvaluationResponse mapToResponse(EvaluationTask task) {
        if (task == null) {
            return null;
        }
        AsyncEvaluationResponse.TaskStatus status = switch (task.getStatus()) {
            case PENDING -> AsyncEvaluationResponse.TaskStatus.PENDING;
            case COMPLETED -> AsyncEvaluationResponse.TaskStatus.COMPLETED;
            case FAILED -> AsyncEvaluationResponse.TaskStatus.FAILED;
        };

        DeepSeekEvalResult result = null;
        if (status == AsyncEvaluationResponse.TaskStatus.COMPLETED && task.getResult() != null) {
            try {
                result = objectMapper.readValue(task.getResult(), DeepSeekEvalResult.class);
            } catch (Exception e) {
                log.warn("Failed to parse result for task {}", task.getId());
            }
        }

        return AsyncEvaluationResponse.builder()
                .taskId(task.getId())
                .status(status)
                .progress(status == AsyncEvaluationResponse.TaskStatus.COMPLETED ? 100 : 0)
                .result(result)
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt() != null ? task.getCreatedAt().toLocalDateTime() : null)
                .completedAt(task.getCompletedAt() != null ? task.getCompletedAt().toLocalDateTime() : null)
                .estimatedSecondsRemaining(status == AsyncEvaluationResponse.TaskStatus.COMPLETED ? 0 : null)
                .build();
    }
}
