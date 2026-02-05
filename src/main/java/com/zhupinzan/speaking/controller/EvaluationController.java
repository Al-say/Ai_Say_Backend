package com.zhupinzan.speaking.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.EvaluationRequest;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.service.AsyncEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final AsyncEvaluationService evaluationService;

    // 🚦 限流器：每秒仅允许 2 个请求（根据你的 AI 额度调整）
    private final RateLimiter rateLimiter = RateLimiter.create(2.0);

    // 1. 提交评估（带限流 + 身份绑定）
    @PostMapping
    public ResponseEntity<?> submitEvaluation(
            @RequestBody EvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 🛑 限流检查
        if (!rateLimiter.tryAcquire()) {
            return ResponseEntity.status(429).body(Map.of("error", "Too many requests, please slow down."));
        }

        String taskId = evaluationService.submitTask(request, userDetails.getUsername());
        return ResponseEntity.ok(Map.of("taskId", taskId, "status", "SUBMITTED"));
    }

    // 2. 查询任务状态
    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskStatus(
            @PathVariable String taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        EvaluationTask task = evaluationService.getTaskResult(taskId);
        
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 🔒 安全检查：只能看自己的任务
        if (!task.getUserIdentity().equals(userDetails.getUsername())) {
            return ResponseEntity.status(403).body("Access denied");
        }

        // 如果完成了，返回结果；否则返回状态
        if (task.getStatus() == AsyncEvaluationResponse.TaskStatus.COMPLETED) {
            // 这里可以直接把 resultJson 字符串返回，或者解析成对象
            // 为了简单，我们先返回整个 Task 对象，客户端自己解析 resultJson
            return ResponseEntity.ok(task);
        } else {
            return ResponseEntity.ok(Map.of("id", taskId, "status", task.getStatus()));
        }
    }

    // 3. 📜 查询历史记录（新增）
    @GetMapping("/history")
    public ResponseEntity<List<EvaluationTask>> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        List<EvaluationTask> history = evaluationService.getUserHistory(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }
}