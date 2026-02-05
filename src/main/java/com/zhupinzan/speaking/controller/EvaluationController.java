package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.EvaluationRequest;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.service.AsyncEvaluationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final AsyncEvaluationService evaluationService;

    // 1. 提交评估（带限流 + 身份绑定）
    @PostMapping
    public ResponseEntity<AsyncEvaluationResponse> submitEvaluation(
            @Valid @RequestBody EvaluationRequest request
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdentity = authentication != null ? authentication.getName() : "anonymous";

        UserPersona persona = UserPersona.valueOf(request.getPersona().toUpperCase());
        AsyncEvaluationResponse response = evaluationService.submitEvaluation(
                persona, request.getScene(), request.getTranscript(), userIdentity);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // 2. 查询任务状态
    @GetMapping("/{taskId}")
    public ResponseEntity<AsyncEvaluationResponse> getTaskStatus(@PathVariable String taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdentity = authentication != null ? authentication.getName() : "anonymous";
        AsyncEvaluationResponse response = evaluationService.getTaskStatus(taskId, userIdentity);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 3. 📜 查询历史记录（新增）
    @GetMapping("/history")
    public ResponseEntity<List<EvaluationTask>> getHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdentity = authentication != null ? authentication.getName() : "anonymous";
        List<EvaluationTask> history = evaluationService.getUserHistory(userIdentity);
        return ResponseEntity.ok(history);
    }
}
