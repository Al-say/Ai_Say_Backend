package com.zhupinzan.speaking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.EvaluationRequest;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
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
            // 假设 deepSeekService.evaluate 返回的是一个对象，我们转成 JSON 存起来
            Object assessmentResult = deepSeekService.evaluate(request.getTranscript());

            task.setResultJson(objectMapper.writeValueAsString(assessmentResult));
            task.setStatus(AsyncEvaluationResponse.TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Task failed: {}", taskId, e);
            task.setStatus(AsyncEvaluationResponse.TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        } finally {
            // 3. 💾 最终状态落库
            taskRepository.save(task);
        }
    }

    // 提交任务
    public String submitTask(EvaluationRequest request, String userIdentity) {
        String taskId = UUID.randomUUID().toString();

        // 1. 先在数据库占个位
        EvaluationTask task = new EvaluationTask();
        task.setId(taskId);
        task.setUserIdentity(userIdentity);
        task.setTranscript(request.getTranscript());
        task.setStatus(AsyncEvaluationResponse.TaskStatus.PENDING);
        taskRepository.save(task);

        // 2. 触发异步处理
        processEvaluation(taskId, request);

        return taskId;
    }

    // 获取单个任务详情
    public EvaluationTask getTaskResult(String taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    // 获取用户历史
    public List<EvaluationTask> getUserHistory(String userIdentity) {
        return taskRepository.findByUserIdentityOrderByCreatedAtDesc(userIdentity);
    }
}