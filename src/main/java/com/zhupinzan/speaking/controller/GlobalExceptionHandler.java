package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.config.RequestIdFilter;
import com.zhupinzan.speaking.model.ErrorCode;
import com.zhupinzan.speaking.model.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * 全局异常处理器
 *
 * 【整体设计理念和架构】
 * 本异常处理器采用Spring的@RestControllerAdvice注解实现全局异常拦截，实现了以下设计原则：
 * 1. 统一异常处理：将散落在各处的异常处理逻辑集中管理，避免重复代码
 * 2. 分层异常处理：按照异常类型进行分类处理，不同类型的异常采用不同的处理策略
 * 3. 标准化响应：所有异常响应都遵循统一的JSON格式，包含错误码、错误信息、请求ID等
 * 4. 可观测性集成：通过MDC传递请求ID，便于日志追踪和问题排查
 *
 * 【异常处理层次结构】
 * - 系统级异常：如数据库异常、网络异常等，统一返回500错误
 * - 业务异常：如参数校验失败、权限不足等，返回对应的HTTP状态码
 * - 第三方服务异常：如AWS S3存储异常、音频转码异常等，返回特定的错误码
 * - 兜底异常：捕获所有未处理的异常，确保系统稳定性
 *
 * 【错误码映射策略】
 * - HTTP 200: 成功响应（不在异常处理器中处理）
 * - HTTP 400: 参数错误（BAD_REQUEST）
 * - HTTP 401: 认证失败（UNAUTHORIZED）
 * - HTTP 403: 权限不足（FORBIDDEN）
 * - HTTP 404: 资源不存在（NOT_FOUND）
 * - HTTP 500: 服务器内部错误（INTERNAL_ERROR）
 * - 业务自定义错误码：如存储错误、音频转码错误等
 *
 * 【业务异常和系统异常的处理区别】
 * 业务异常：
 * - 通常由业务逻辑主动抛出
 * - 错误信息对用户友好
 * - HTTP状态码反映业务语义
 * - 可能包含详细的错误原因和解决建议
 *
 * 系统异常：
 * - 通常由框架或第三方服务抛出
 * - 错误信息经过脱敏处理
 * - 统一返回500或503状态码
 * - 详细错误信息记录到日志，不直接返回给用户
 *
 * 【日志记录和监控集成】
 * - 使用SLF4J记录异常日志，按ERROR级别记录未处理的异常
 * - 通过MDC传递请求ID，实现全链路追踪
 * - 关键异常记录堆栈信息，便于问题定位
 * - 集成监控系统，可以接入Prometheus、ELK等工具
 *
 * 【性能考虑和优化措施】
 * - 异常处理方法尽量轻量级，避免在异常处理中进行复杂计算
 * - 使用switch表达式快速映射HTTP状态码到错误码
 * - 异常响应JSON对象使用builder模式创建，减少对象创建开销
 * - 日志记录使用参数化形式，避免字符串拼接开销
 *
 * 【与业务逻辑的集成关系】
 * - 通过注解方式与Spring MVC框架深度集成
 * - 不侵入业务代码，业务逻辑只需正常抛出异常
 * - 支持自定义异常类型，便于业务扩展
 * - 与DTO层配合，确保响应格式的一致性
 *
 * 【扩展性和维护性考虑】
 * - 采用策略模式，每种异常类型对应一个处理方法
 * - 新增异常类型只需添加新的@ExceptionHandler方法
 * - 错误码集中管理在ErrorCode枚举中，便于维护
 * - 支持继承和重写，便于子类定制异常处理逻辑
 *
 * @author System
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 获取当前请求的请求ID
     *
     * 【设计说明】
     * 通过MDC（Mapped Diagnostic Context）获取请求ID，实现日志追踪。
     * 请求ID由RequestIdFilter在请求进入时设置，确保整个请求链路中的日志都能关联到同一个请求ID。
     *
     * @return 请求ID字符串，如果不存在则返回null
     */
    private String getRequestId() {
        return MDC.get(RequestIdFilter.MDC_KEY);
    }

    /**
     * 处理Spring内置的响应状态异常
     *
     * 【处理策略】
     * 专门处理Spring框架抛出的ResponseStatusException，包括以下常见HTTP状态：
     * - 401 UNAUTHORIZED: 认证失败
     * - 403 FORBIDDEN: 权限不足
     * - 404 NOT_FOUND: 资源不存在
     * - 400 BAD_REQUEST: 请求参数错误
     *
     * 【映射规则】
     * 使用switch表达式将HTTP状态码映射到对应的业务错误码，确保：
     * 1. HTTP状态码和业务错误码语义一致
     * 2. 错误码在ErrorCode枚举中定义，便于维护
     * 3. 默认情况返回BAD_REQUEST，避免未映射状态码导致异常
     *
     * 【异常信息处理】
     * - 使用e.getReason()获取Spring框架提供的错误原因
     * - 优先使用框架提供的错误信息，保证信息准确性
     *
     * @param e ResponseStatusException异常对象
     * @paramreq HttpServletRequest对象，用于获取请求信息
     * @return 标准化的错误响应实体
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException e, HttpServletRequest req) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        ErrorCode code = switch (status) {
            case UNAUTHORIZED -> ErrorCode.UNAUTHORIZED;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            case BAD_REQUEST -> ErrorCode.BAD_REQUEST;
            default -> ErrorCode.BAD_REQUEST;
        };
        return buildResponse(status, code, e.getReason(), req);
    }

    /**
     * 处理非法参数异常
     *
     * 【适用场景】
     * 主要处理以下情况抛出的IllegalArgumentException：
     * - 方法参数类型不匹配
     * - 参数值超出允许范围
     * - 业务逻辑中的参数验证失败
     * - 对象状态不合法
     *
     * 【处理策略】
     * - 统一返回400 BAD_REQUEST状态码
     * - 使用ErrorCode.BAD_REQUEST错误码
     * - 直接返回异常的原始消息，保持错误信息的透明度
     *
     * 【最佳实践】
     * 1. 在业务方法中，应优先使用@Validated注解进行参数校验
     * 2. 对于复杂的参数校验，建议使用自定义异常替代IllegalArgumentException
     * 3. 错误信息应该清晰明确，便于调试和用户理解
     *
     * @param e IllegalArgumentException异常对象
     * @paramreq HttpServletRequest对象
     * @return 参数错误的响应实体
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST, e.getMessage(), req);
    }

    /**
     * 兜底异常处理器
     *
     * 【设计原则】
     * 作为异常处理的最后一道防线，捕获所有未在其他方法中处理的异常。
     * 这是防御性编程的重要体现，确保系统不会因为未处理的异常而崩溃。
     *
     * 【处理策略】
     * - 记录异常堆栈：使用log.error记录完整的异常堆栈，便于问题定位
     * - 返回通用错误：对外返回通用的"Internal Server Error"消息，避免信息泄露
     * - 隐藏敏感信息：不直接返回异常的原始消息，防止系统信息泄露
     * - 记录请求ID：通过MDC中的请求ID，将异常日志与具体请求关联
     *
     * 【错误处理原则】
     * 1. 安全性：不向用户暴露系统内部细节和敏感信息
     * 2. 可观测性：记录足够的日志信息供开发和运维人员排查问题
     * 3. 一致性：所有未处理的异常都返回相同的响应格式
     * 4. 性能：避免在异常处理中进行复杂操作，影响系统性能
     *
     * 【监控告警】
     * 此方法被触发通常意味着系统存在bug或异常情况，建议：
     * - 接入监控系统，对此类异常进行告警
     * - 设置告警阈值，如每分钟超过N次触发告警
     * - 结合日志分析，快速定位和解决问题
     *
     * @param e 未处理的异常对象
     * @paramreq HttpServletRequest对象
     * @return 服务器内部错误响应实体
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception e, HttpServletRequest req) {
        log.error("[{}] Unhandled exception: ", getRequestId(), e); // 打印堆栈以便排查
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Internal Server Error", req);
    }

    /**
     * 处理AWS SDK存储服务异常
     *
     * 【适用场景】
     * 处理与AWS S3等存储服务交互时可能出现的各种异常：
     * - 网络连接问题
     * - 认证失败
     * - 权限不足
     * - 服务不可用
     * - 配置错误
     *
     * 【处理策略】
     * - 错误分类：使用ErrorCode.STORAGE_ERROR专门标识存储相关错误
     * - 用户友好消息：返回"文件存储服务暂时不可用"而非技术细节
     * - 详细日志：记录完整的异常堆栈，便于运维人员排查
     * - 服务降级：提示用户稍后重试，避免重试导致雪崩
     *
     * 【故障恢复建议】
     * 1. 检查AWS服务状态和配置
     * 2. 验证访问密钥和权限设置
     * 3. 检查网络连接和防火墙设置
     * 4. 考虑实现本地缓存机制作为降级方案
     *
     * 【监控指标】
     * 建议监控以下指标：
     * - 存储服务异常发生频率
     * - 异常持续时间
     * - 影响的请求量
     * - 服务恢复时间
     *
     * @param e AWS SDK异常对象
     * @paramreq HttpServletRequest对象
     * @return 存储服务错误响应实体
     */
    @ExceptionHandler(software.amazon.awssdk.core.exception.SdkException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageError(Exception e, HttpServletRequest req) {
        log.error("Storage Service Error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(ErrorCode.STORAGE_ERROR, "文件存储服务暂时不可用", getRequestId(), req.getRequestURI()));
    }

    /**
     * 处理音频转码服务异常
     *
     * 【业务背景】
     * 音频转码是系统的核心功能之一，涉及音频格式转换、参数调整等复杂操作。
     * 可能遇到的异常包括：
     * - 音频格式不支持
     * - 转码参数无效
     * - 第三方转码服务失败
     * - 资源不足（内存、磁盘空间等）
     *
     * 【处理策略】
     * - 错误隔离：使用ErrorCode.AUDIO_TRANSCODE_ERROR专门标识音频处理错误
     * - 明确提示：返回"音频转码失败"，让用户清楚问题所在
     * - 详细日志：记录请求ID和完整堆栈，便于技术排查
     * - 重试建议：客户端可以实现自动重试机制（最多重试3次）
     *
     * 【故障排查步骤】
     * 1. 检查输入音频格式和参数是否合法
     * 2. 验证转码服务配置是否正确
     * 3. 检查系统资源使用情况
     * 4. 查看转码服务日志获取详细信息
     * 5. 考虑降级方案：返回原始音频或生成文本提示
     *
     * 【性能优化】
     * 1. 对音频文件大小进行限制，避免大文件转码
     * 2. 实现异步转码机制，避免阻塞主线程
     * 3. 添加转码超时机制，防止长时间等待
     * 4. 缓存常用音频格式转换结果
     *
     * @param e 音频转码异常对象
     * @paramreq HttpServletRequest对象
     * @return 音频转码错误响应实体
     */
    @ExceptionHandler(com.zhupinzan.speaking.service.audio.AudioTranscodeService.AudioTranscodeException.class)
    public ResponseEntity<ApiErrorResponse> handleTranscode(Exception e, HttpServletRequest req) {
        log.error("[{}] Audio transcode failed", getRequestId(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.AUDIO_TRANSCODE_ERROR, "音频转码失败", req);
    }

    /**
     * 构建标准化的错误响应实体
     *
     * 【设计原则】
     * 作为公共方法，确保所有异常响应都遵循统一的格式标准。
     * 这是实现系统一致性、可维护性的重要手段。
     *
     * 【响应格式】
     * 返回的ApiErrorResponse包含以下字段：
     * - code: 业务错误码，用于程序识别和分类
     * - message: 用户友好的错误提示信息
     * - requestId: 请求唯一标识，便于追踪和排查
     * - path: 请求路径，标识哪个接口出现问题
     * - timestamp: 响应时间戳（在ApiErrorResponse内部处理）
     *
     * 【HTTP状态码映射】
     * - 200: 成功（不在异常处理器中使用）
     * - 400: 客户端请求错误
     * - 401: 未授权
     * - 403: 禁止访问
     * - 404: 资源不存在
     * - 500: 服务器内部错误
     * - 503: 服务不可用
     *
     * 【最佳实践】
     * 1. 状态码应与业务错误码语义保持一致
     * 2. 错误信息应简洁明了，避免技术术语
     * 3. 请求ID必须包含，实现全链路追踪
     * 4. 不要在响应中暴露敏感信息
     * 5. 保持响应体大小合理，避免过大
     *
     * @param status HTTP状态码，表示响应的类别
     * @param code 业务错误码，用于错误分类和处理
     * @param msg 用户友好的错误提示信息
     * @paramreq HttpServletRequest对象，用于获取请求路径等信息
     * @return 标准化的错误响应实体
     */
  private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, ErrorCode code, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(code, msg, getRequestId(), req.getRequestURI()));
    }
}
