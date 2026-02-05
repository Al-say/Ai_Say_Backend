package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.EvaluationRequest;
import com.zhupinzan.speaking.service.AsyncEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 口语评估 MVP Controller
 * <p>
 * <b>设计理念：</b>最小可行产品（MVP），专注核心评估流程。
 * <p>
 * <b>核心接口：</b><br>
 * • POST /api/v1/evaluate - 提交评估任务（异步）<br>
 * • GET /api/v1/evaluate/{taskId} - 查询任务状态<br>
 * • DELETE /api/v1/evaluate/{taskId} - 清理任务资源
 * <p>
 * <b>工作流程：</b><br>
 * 1. iOS 客户端录音 → 上传音频<br>
 * 2. 调用百度 ASR → 得到 transcript<br>
 * 3. POST /evaluate → 返回 TaskID<br>
 * 4. 轮询 GET /evaluate/{taskId} → 获取结果<br>
 * 5. 展示评分和反馈
 * <p>
 * <b>设计优势：</b><br>
 * • 异步处理：避免 HTTP 超时<br>
 * • 状态追踪：客户端可控进度<br>
 * • 清晰契约：标准 REST API<br>
 * • 容错机制：失败状态透明返回
 * <p>
 * <b>安全考虑：</b><br>
 * ⚠️ 生产环境需要添加：<br>
 * • 用户认证（JWT Token）<br>
 * • 请求频率限制（防刷）<br>
 * • 输入长度校验<br>
 * • SQL 注入防护
 *
 * @author system
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/evaluate")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {

    private final AsyncEvaluationService asyncEvaluationService;

    /**
     * 提交口语评估任务（异步）
     * <p>
     * <b>请求示例：</b>
     * <pre>
     * POST /api/v1/evaluate
     * {
     *   "persona": "EXAM_PREP",
     *   "scene": "job_interview",
     *   "transcript": "I have five years of experience in software development...",
     *   "userId": "user123",
     *   "async": true
     * }
     * </pre>
     * <p>
     * <b>响应示例：</b>
     * <pre>
     * {
     *   "taskId": "550e8400-e29b-41d4-a716-446655440000",
     *   "status": "PENDING",
     *   "progress": 0,
     *   "estimatedSecondsRemaining": 15,
     *   "createdAt": "2026-02-05T23:30:00"
     * }
     * </pre>
     *
     * @param request 评估请求对象（已验证）
     * @return 任务响应（包含 TaskID）
     */
    @PostMapping
    public ResponseEntity<AsyncEvaluationResponse> submitEvaluation(
            @Valid @RequestBody EvaluationRequest request) {

        log.info("收到评估请求: persona={}, scene={}, transcriptLength={}",
                request.getPersona(), request.getScene(), request.getTranscript().length());

        try {
            // 1. 参数转换和验证
            UserPersona persona = UserPersona.valueOf(request.getPersona().toUpperCase());

            // 2. 提交异步任务
            AsyncEvaluationResponse response = asyncEvaluationService.submitEvaluation(
                    persona,
                    request.getScene(),
                    request.getTranscript()
            );

            log.info("任务创建成功: taskId={}", response.getTaskId());

            // 3. 返回任务信息
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("无效的 persona 参数: {}", request.getPersona());
            return ResponseEntity.badRequest().body(
                    AsyncEvaluationResponse.failed(null, "无效的 persona 类型: " + request.getPersona())
            );
        } catch (Exception e) {
            log.error("提交评估任务失败", e);
            return ResponseEntity.internalServerError().body(
                    AsyncEvaluationResponse.failed(null, "服务内部错误: " + e.getMessage())
            );
        }
    }

    /**
     * 查询任务状态
     * <p>
     * <b>轮询建议：</b><br>
     * • 间隔时间：2-3 秒<br>
     * • 最大次数：20 次（约 1 分钟）<br>
     * • 超时策略：提示用户稍后重试
     * <p>
     * <b>响应示例（处理中）：</b>
     * <pre>
     * {
     *   "taskId": "550e8400-e29b-41d4-a716-446655440000",
     *   "status": "PROCESSING",
     *   "progress": 60,
     *   "estimatedSecondsRemaining": 6
     * }
     * </pre>
     * <p>
     * <b>响应示例（已完成）：</b>
     * <pre>
     * {
     *   "taskId": "550e8400-e29b-41d4-a716-446655440000",
     *   "status": "COMPLETED",
     *   "progress": 100,
     *   "result": {
     *     "status": "ok",
     *     "overallScore": 85,
     *     "metrics": { "fluency": 88, "grammar": 82, ... },
     *     "feedback": { ... }
     *   },
     *   "completedAt": "2026-02-05T23:30:15"
     * }
     * </pre>
     *
     * @param taskId 任务 ID（从提交接口获取）
     * @return 任务状态和结果
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<AsyncEvaluationResponse> getTaskStatus(@PathVariable String taskId) {
        log.debug("查询任务状态: taskId={}", taskId);

        AsyncEvaluationResponse response = asyncEvaluationService.getTaskStatus(taskId);

        // 根据任务状态返回不同的 HTTP 状态码
        return switch (response.getStatus()) {
            case COMPLETED -> ResponseEntity.ok(response);
            case PROCESSING, PENDING -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            case FAILED -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        };
    }

    /**
     * 删除任务（清理资源）
     * <p>
     * 客户端获取结果后应主动调用此接口，避免服务器内存泄漏。
     * </p>
     *
     * @param taskId 任务 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        log.info("删除任务: taskId={}", taskId);
        asyncEvaluationService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 健康检查接口
     * <p>用于服务可用性监控</p>
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Evaluation Service is running");
    }
}
