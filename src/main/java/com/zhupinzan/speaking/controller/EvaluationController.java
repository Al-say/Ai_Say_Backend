package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.EvaluationRequest;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.service.AsyncEvaluationService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final AsyncEvaluationService evaluationService;

    // 1. 提交评估（带限流 + 身份绑定）
    @PostMapping
    public ResponseEntity<AsyncEvaluationResponse> submitEvaluation(
            @Valid @RequestBody EvaluationRequest request,
            @CurrentUser CurrentUserInfo user
    ) {
        Long ownerUserId = requireUserId(user);

        UserPersona persona = parsePersona(request.getPersona());
        AsyncEvaluationResponse response = evaluationService.submitEvaluation(
                persona, request.getScene(), request.getTranscript(), ownerUserId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    // 2. 查询任务状态
    @GetMapping("/{taskId}")
    public ResponseEntity<AsyncEvaluationResponse> getTaskStatus(
            @PathVariable String taskId,
            @CurrentUser CurrentUserInfo user
    ) {
        Long ownerUserId = requireUserId(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> legacyOwnerKeys = buildLegacyOwnerKeys(user, authentication);

        AsyncEvaluationResponse response = evaluationService.getTaskStatus(taskId, ownerUserId, legacyOwnerKeys);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 3. 📜 查询历史记录（新增）
    @GetMapping("/history")
    public ResponseEntity<List<EvaluationTask>> getHistory(@CurrentUser CurrentUserInfo user) {
        Long ownerUserId = requireUserId(user);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> legacyOwnerKeys = buildLegacyOwnerKeys(user, authentication);

        List<EvaluationTask> history = evaluationService.getUserHistory(ownerUserId, legacyOwnerKeys);
        return ResponseEntity.ok(history != null ? history : Collections.emptyList());
    }

    private Long requireUserId(CurrentUserInfo user) {
        if (user == null || user.userId() == null || user.userId() <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return user.userId();
    }

    private Set<String> buildLegacyOwnerKeys(CurrentUserInfo user, Authentication authentication) {
        Set<String> keys = new LinkedHashSet<>();

        if (authentication != null && authentication.getName() != null
                && !authentication.getName().isBlank()) {
            keys.add(authentication.getName());
        }

        if (user != null && user.appleSub() != null && !user.appleSub().isBlank()) {
            keys.add(user.appleSub());
        }

        return keys;
    }

    private UserPersona parsePersona(String value) {
        try {
            return UserPersona.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String allowedValues = String.join(", ",
                    Arrays.stream(UserPersona.values()).map(Enum::name).toList());
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "persona 参数无效，可选值: " + allowedValues,
                    e
            );
        }
    }
}
