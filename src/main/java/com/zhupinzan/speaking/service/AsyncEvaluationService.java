package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.AsyncEvaluationResponse;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.repository.EvaluationTaskRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 异步评估任务管理服务
 * <p>
 * <b>核心职责：</b>管理长时间运行的 AI 评估任务，避免 HTTP 超时。
 * <p>
 * <b>架构设计：</b><br>
 * • 任务提交：客户端发起评估请求，立即返回 TaskID<br>
 * • 后台处理：Spring 异步线程池执行 AI 评估<br>
 * • 状态轮询：客户端定期查询任务状态<br>
 * • 结果获取：任务完成后返回完整评估结果
 * <p>
 * <b>技术实现：</b><br>
 * • @Async：Spring 异步方法，自动在线程池中执行<br>
 * • CompletableFuture：异步编程模型<br>
 * • UUID：全局唯一的任务标识符
 * <p>
 * <b>性能考虑：</b><br>
 * • 任务持久化：使用数据库存储任务状态，支持重启恢复<br>
 * • 并发控制：限制同时执行的任务数<br>
 * • 资源隔离：避免任务间相互影响
 * <p>
 * <b>注意事项：</b><br>
 * ⚠️ 需要配置合理的线程池大小<br>
 * ⚠️ 考虑任务持久化和恢复机制
 *
 * @author system
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEvaluationService {

    private final DeepSeekEvalService deepSeekEvalService;
    private final EvaluationTaskRepository evaluationTaskRepository;

    /**
     * 提交异步评估任务
     * <p>
     * <b>工作流程：</b><br>
     * 1. 生成唯一 TaskID<br>
     * 2. 🔐 绑定任务与用户<br>
     * 3. 创建 PENDING 状态的任务<br>
     * 4. 异步执行 AI 评估<br>
     * 5. 立即返回 TaskID 给客户端
     * </p>
     *
     * @param persona      用户画像类型
     * @param scene        评估场景
     * @param transcript   转写文本
     * @param userIdentity 🔐 用户身份（email/username）
     * @return 任务响应对象（包含 TaskID）
     */
    public AsyncEvaluationResponse submitEvaluation(UserPersona persona, String scene, 
                                                     String transcript, String userIdentity) {
        // 1. 生成唯一任务 ID
        String taskId = UUID.randomUUID().toString();
        log.info("🔐 创建异步评估任务: taskId={}, owner={}, persona={}, scene={}", 
                 taskId, userIdentity, persona, scene);

        // 2. 构建并保存任务实体
        EvaluationTask evaluationTask = EvaluationTask.builder()
                .id(taskId)
                .userIdentity(userIdentity)
                .persona(persona)
                .scene(scene)
                .transcript(transcript)
                .status(AsyncEvaluationResponse.TaskStatus.PENDING)
                .progress(0)
                .build();
        evaluationTaskRepository.save(evaluationTask);

        // 3. 异步执行评估（不阻塞当前线程）
        executeEvaluationAsync(taskId, persona, scene, transcript);

        // 4. 立即返回任务信息
        return AsyncEvaluationResponse.pending(taskId);
    }

    /**
     * 异步执行 AI 评估
     * <p>
     * <b>@Async 注解作用：</b><br>
     * • 方法在独立线程池中执行<br>
     * • 不阻塞调用方<br>
     * • 自动管理线程生命周期
     * </p>
     * <p>
     * <b>异常处理：</b><br>
     * • 捕获所有异常，避免线程意外终止<br>
     * • 将错误信息记录到任务状态中<br>
     * • 标记任务为 FAILED 状态
     * </p>
     *
     * @param taskId     任务 ID
     * @param persona    用户画像
     * @param scene      评估场景
     * @param transcript 转写文本
     */
    @Async("taskExecutor")  // 使用自定义线程池（需在配置中定义）
    public CompletableFuture<Void> executeEvaluationAsync(String taskId, UserPersona persona, 
                                                           String scene, String transcript) {
        try {
            log.info("开始处理异步评估: taskId={}", taskId);

            // 更新状态为 PROCESSING
            updateTaskStatus(taskId, AsyncEvaluationResponse.processing(taskId, 30));

            // 调用 DeepSeek AI 进行评估（可能耗时 5-30 秒）
            DeepSeekEvalResult result = deepSeekEvalService.evaluate(persona, scene, transcript);

            // 更新状态为 COMPLETED
            updateTaskStatus(taskId, AsyncEvaluationResponse.completed(taskId, result));

            log.info("异步评估完成: taskId={}, overallScore={}", taskId, result.overallScore());

        } catch (Exception e) {
            log.error("异步评估失败: taskId={}", taskId, e);
            updateTaskStatus(taskId, AsyncEvaluationResponse.failed(taskId, e.getMessage()));
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 查询任务状态
     * <p>
     * 客户端定期调用此方法（如每 2 秒轮询一次）检查任务进度。
     * 🔐 只有任务所有者才能查询。
     * </p>
     *
     * @param taskId       任务 ID
     * @param userIdentity 🔐 请求者身份
     * @return 任务响应对象（包含状态和结果）
     */
    public AsyncEvaluationResponse getTaskStatus(String taskId, String userIdentity) {
        return evaluationTaskRepository.findById(taskId)
                .map(task -> {
                    // 🔐 验证任务所有权
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
                            .createdAt(task.getCreatedAt() != null ? LocalDateTime.ofInstant(task.getCreatedAt(), java.time.ZoneOffset.UTC) : null) // Convert Instant to LocalDateTime
                            .completedAt(task.getCompletedAt())
                            .build();
                })
                .orElseGet(() -> {
                    log.warn("任务不存在或已过期: taskId={}", taskId);
                    return AsyncEvaluationResponse.failed(taskId, "任务不存在或已过期");
                });
    }

    /**
     * 删除任务（清理资源）
     * <p>
     * 客户端获取结果后应主动调用此方法，释放服务器资源。
     * 🔐 只有任务所有者才能删除。
     * </p>
     *
     * @param taskId       任务 ID
     * @param userIdentity 🔐 请求者身份
     * @return true 如果删除成功，false 如果无权或任务不存在
     */
    public boolean deleteTask(String taskId, String userIdentity) {
        return evaluationTaskRepository.findById(taskId)
                .map(task -> {
                    // 🔐 验证任务所有权
                    if (!task.getUserIdentity().equals(userIdentity)) {
                        log.warn("🚨 删除拒绝: taskId={}, owner={}, requestedBy={}", taskId, task.getUserIdentity(), userIdentity);
                        return false;
                    }
                    evaluationTaskRepository.delete(task);
                    log.info("任务已删除: taskId={}, owner={}", taskId, userIdentity);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("任务不存在或已过期: taskId={}", taskId);
                    return false;
                });
    }

    /**
     * 更新任务状态（内部方法），从数据库加载任务，更新字段并保存
     */
    private void updateTaskStatus(String taskId, AsyncEvaluationResponse response) {
        evaluationTaskRepository.findById(taskId).ifPresent(task -> {
            task.setStatus(response.getStatus());
            task.setProgress(response.getProgress());
            task.setErrorMessage(response.getErrorMessage());
            task.setResult(response.getResult());
            task.setCompletedAt(response.getCompletedAt()); // Set completedAt if available
            evaluationTaskRepository.save(task);
        });
    }

    // /**
    //  * 清理过期任务（定时任务）
    //  * <p>
    //  * <b>建议配置：</b><br>
    //  * • 每 10 分钟执行一次<br>
    //  * • 清理 1 小时前创建的任务<br>
    //  * • 记录清理日志
    //  * </p>
    //  */
    // @Scheduled(fixedRate = 600000) // 每 10 分钟
    // public void cleanupExpiredTasks() {
    //     // TODO: 如果需要清理数据库中的过期任务，请实现数据库清理逻辑
    //     log.info("执行过期任务清理 (目前未实现数据库清理)");
    // }
}
