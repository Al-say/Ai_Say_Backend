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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEvaluationService {

    private static final String OWNER_PREFIX = "USER:";

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
    public AsyncEvaluationResponse submitEvaluation(
            com.zhupinzan.speaking.model.UserPersona persona,
            String scene,
            String transcript,
            Long ownerUserId
    ) {
        if (ownerUserId == null || ownerUserId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效的用户身份");
        }

        String taskId = UUID.randomUUID().toString();

        // 1. 先在数据库占个位
        EvaluationTask task = new EvaluationTask();
        task.setId(taskId);
        task.setOwnerUserId(ownerUserId);
        task.setUserIdentity(toCanonicalOwnerKey(ownerUserId));
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
    public AsyncEvaluationResponse getTaskStatus(String taskId, Long ownerUserId, Set<String> legacyOwnerKeys) {
        EvaluationTask task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return null;
        }
        if (!isOwner(task, ownerUserId, legacyOwnerKeys)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该任务");
        }
        return mapToResponse(task);
    }

    // 获取用户历史
    public List<EvaluationTask> getUserHistory(Long ownerUserId, Set<String> legacyOwnerKeys) {
        if (ownerUserId == null || ownerUserId <= 0) {
            return List.of();
        }

        List<EvaluationTask> result = new ArrayList<>(taskRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId));

        Set<String> compatibilityKeys = new LinkedHashSet<>(legacyOwnerKeys == null ? Set.of() : legacyOwnerKeys);
        compatibilityKeys.add(toCanonicalOwnerKey(ownerUserId));

        if (!compatibilityKeys.isEmpty()) {
            List<EvaluationTask> legacy = taskRepository.findByUserIdentityInOrderByCreatedAtDesc(
                    new ArrayList<>(compatibilityKeys));
            result.addAll(legacy);
        }

        // 去重 + 统一倒序
        Map<String, EvaluationTask> dedup = new LinkedHashMap<>();
        for (EvaluationTask task : result) {
            dedup.putIfAbsent(task.getId(), task);
        }

        return dedup.values().stream()
                .sorted(Comparator.comparing(EvaluationTask::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private boolean isOwner(EvaluationTask task, Long ownerUserId, Set<String> legacyOwnerKeys) {
        if (ownerUserId == null || ownerUserId <= 0 || task == null) {
            return false;
        }

        if (task.getOwnerUserId() != null) {
            return ownerUserId.equals(task.getOwnerUserId());
        }

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.add(toCanonicalOwnerKey(ownerUserId));
        if (legacyOwnerKeys != null) {
            allKeys.addAll(legacyOwnerKeys);
        }

        return task.getUserIdentity() != null && allKeys.contains(task.getUserIdentity());
    }

    private String toCanonicalOwnerKey(Long ownerUserId) {
        return OWNER_PREFIX + ownerUserId;
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
