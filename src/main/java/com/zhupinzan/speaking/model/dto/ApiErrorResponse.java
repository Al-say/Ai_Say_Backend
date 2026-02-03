package com.zhupinzan.speaking.model.dto;

import static com.zhupinzan.speaking.model.ErrorCode.*;
import com.zhupinzan.speaking.model.ErrorCode;

import java.time.OffsetDateTime;

/**
 * API 错误响应数据传输对象
 *
 * <p><b>作用：</b>统一封装 API 错误响应的格式，提供标准化的错误信息传递。
 * 确保所有 API 错误响应都遵循一致的结构，便于前端统一处理和错误追踪。</p>
 *
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>使用 Java Record 确保数据不可变性</li>
 *   <li>统一的成功状态标识（false）</li>
 *   <li>使用 ErrorCode 枚举确保错误代码标准化</li>
 *   <li>包含请求追踪信息（requestId）</li>
 *   <li>记录错误发生时间和路径</li>
 *   <li>提供工厂方法简化创建</li>
 * </ul>
 *
 * <p><b>业务场景：</b></p>
 * <ul>
 *   <li>API 参数验证失败</li>
 *   <li>业务逻辑错误</li>
 *   <li>系统异常处理</li>
 *   <li>权限验证失败</li>
 *   <li>资源不存在或访问受限</li>
 *   <li>第三方服务调用失败</li>
 * </ul>
 *
 * <p><b>数据关系：</b></p>
 * <ul>
 *   <li>success：始终为 false，标识错误响应</li>
 *   <li>code：错误代码，对应 ErrorCode 枚举</li>
 *   <li>message：错误消息描述</li>
 *   <li>requestId：请求唯一标识，用于追踪</li>
 *   <li>path：请求路径，便于定位问题</li>
 *   <li>timestamp：错误发生时间</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>success 固定为 false，不传输可节省带宽</li>
 *   <li>ErrorCode 枚举使用字符串值序列化</li>
 *   <li>OffsetDateTime 自动序列化为 ISO-8601 格式</li>
 *   <li>requestId 和 path 保持字符串格式</li>
 * </ul>
 *
 * <p><b>验证规则：</b></p>
 * <ul>
 *   <li>success：固定为 false</li>
 *   <li>code：非空，有效的 ErrorCode 枚举值</li>
 *   <li>message：非空，长度 1-500 字符</li>
 *   <li>requestId：非空，UUID 格式</li>
 *   <li>path：非空，有效的 API 路径</li>
 *   <li>timestamp：非空，当前时间</li>
 * </ul>
 *
 * <p><b>前端交互规范：</b></p>
 * <ul>
 *   <li>错误提示：根据 code 和 message 显示友好提示</li>
 *   <li>错误追踪：记录 requestId 用于技术支持</li>
 *   <li>错误分类：根据 code 区分不同类型错误</li>
 *   <li>重试建议：根据错误类型提供重试建议</li>
 *   <li>日志记录：记录完整的错误响应信息</li>
 * </ul>
 *
 * <p><b>错误代码分类：</b></p>
 * <ul>
 *   <li><b>客户端错误（4xx）</b>：请求参数错误、权限不足、资源不存在</li>
 *   <li><b>服务端错误（5xx）</b>：系统异常、服务不可用、数据库错误</li>
 *   <li><b>业务错误（Bxxx）</b>：业务规则违反、流程异常</li>
 *   <li><b>第三方错误（Txxx）</b>：外部服务调用失败</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public record ApiErrorResponse(
        boolean success,
        ErrorCode code,
        String message,
        String requestId,
        String path,
        OffsetDateTime timestamp
) {

    /**
     * 成功标志
     *
     * <p><b>作用：</b>标识响应类型，错误响应始终为 false</p>
     * <p><b>数据类型：</b>boolean</p>
     * <p><b>固定值：</b>false</p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>前端统一判断响应类型</li>
     *   <li>自动化测试识别错误响应</li>
     *   <li>API 网关路由和过滤</li>
     * </ul>
     * </p>
     */
    public boolean success() {
        return success;
    }

    /**
     * 错误代码
     *
     * <p><b>作用：</b>标准化的错误代码，用于程序化错误处理</p>
     * <p><b>数据类型：</b>ErrorCode 枚举</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>必须是有效的 ErrorCode 枚举值</li>
     *   <li>遵循命名规范</li>
     *   <li>具有明确的业务含义</li>
     * </ul>
     * </p>
     * <p><b>代码结构：</b>
     * <ul>
     *   <li><b>4xxx - HTTP 客户端错误</b>
     *     <ul>
     *       <li>400 - BAD_REQUEST：请求参数错误</li>
     *       <li>401 - UNAUTHORIZED：未认证</li>
     *       <li>403 - FORBIDDEN：权限不足</li>
     *       <li>404 - NOT_FOUND：资源不存在</li>
     *       <li>429 - TOO_MANY_REQUESTS：请求过于频繁</li>
     *     </ul>
     *   </li>
     *   <li><b>5xxx - HTTP 服务端错误</b>
     *     <ul>
     *       <li>500 - INTERNAL_SERVER_ERROR：内部错误</li>
     *       <li>502 - BAD_GATEWAY：网关错误</li>
     *       <li>503 - SERVICE_UNAVAILABLE：服务不可用</li>
     *       <li>504 - GATEWAY_TIMEOUT：网关超时</li>
     *     </ul>
     *   </li>
     *   <li><b>Bxxx - 业务错误</b>
     *     <ul>
     *       <li>B001 - USER_NOT_FOUND：用户不存在</li>
     *       <li>B002 - INVALID_TOKEN：无效令牌</li>
     *       <li>B003 - EVALUATION_FAILED：评估失败</li>
     *       <li>B004 - INSUFFICIENT_BALANCE：余额不足</li>
     *     </ul>
     *   </li>
     *   <li><b>Txxx - 第三方错误</b>
     *     <ul>
     *       <li>T001 - AI_SERVICE_ERROR：AI 服务错误</li>
     *       <li>T002 - ASR_ERROR：语音识别错误</li>
     *       <li>T003 - PAYMENT_ERROR：支付错误</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     */
    public ErrorCode code() {
        return code;
    }

    /**
     * 错误消息
     *
     * <p><b>作用：</b>错误的具体描述信息，用于用户提示和调试</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度 1-500 字符</li>
     *   <li>语言：中文或英文（根据客户端）</li>
     *   <li>格式：清晰、准确、易懂</li>
     * </ul>
     * </p>
     * <p><b>消息设计原则：</b>
     * <ul>
     *   <li>对用户友好：避免技术术语，使用日常语言</li>
     *   <li>准确描述：清楚地说明问题所在</li>
     *   <li>提供指导：告知用户如何解决或下一步</li>
     *   <li>保持简洁：不过于冗长</li>
     *   <li>考虑国际化：支持多语言</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <ul>
     *   <li>客户端友好："请检查网络连接后重试"</li>
     *   <li>技术调试："数据库连接超时，请检查数据库服务状态"</li>
     *   <li>业务指导："您的每日话题已完成，请明天再试"</li>
     * </ul>
     * </p>
     */
    public String message() {
        return message;
    }

    /**
     * 请求ID
     *
     * <p><b>作用：</b>唯一标识一次 HTTP 请求，用于错误追踪和日志关联</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>格式：</b>UUID 格式（8-4-4-4-12）</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>UUID 格式，唯一性保证</li>
     *   <li>生成时机：请求进入网关时</li>
     *   <li>传播：在整个调用链中传递</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>日志关联：将不同服务的日志关联到一次请求</li>
     *   <li>问题排查：通过 requestId 快速定位问题</li>
     *   <li>用户支持：用户提供 requestId 可快速查询问题</li>
     *   <li>性能监控：追踪请求在系统中的完整路径</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>550e8400-e29b-41d4-a716-446655440000</p>
     */
    public String requestId() {
        return requestId;
    }

    /**
     * 请求路径
     *
     * <p><b>作用：</b>记录发生错误的 API 端点，便于定位问题</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>格式：</b>完整的 API 路径，包括查询参数</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>完整的 URL 路径</li>
     *   <li>包含查询参数（如果有）</li>
     *   <li>已脱敏敏感信息</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>问题定位：快速确定哪个接口出现问题</li>
     *   <li>错误分类：按接口分组统计错误</li>
     *   <li>性能分析：识别易出错的接口</li>
     *   <li>监控告警：基于路径设置告警规则</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <ul>
     *   <li>/api/v1/eval/text</li>
     *   <li>/api/v1/topics/2024-01-01</li>
     *   <li>/api/v1/scenes/restaurant_ordering</li>
     * </ul>
     * </p>
     */
    public String path() {
        return path;
    }

    /**
     * 时间戳
     *
     * <p><b>作用：</b>记录错误发生的精确时间</p>
     * <p><b>数据类型：</b>OffsetDateTime</p>
     * <p><b>格式：</b>ISO-8601 带时区的时间戳</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>使用 UTC 时间</li>
     *   <li>精确到毫秒</li>
     *   <li>自动生成，不可修改</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>时间排序：错误按时间顺序排列</li>
     *   <li>时效分析：分析错误的时间分布</li>
     *   <li>问题回溯：确定错误发生的时间点</li>
     *   <li>SLA 计算：计算服务可用性</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>2024-01-01T12:00:00Z</p>
     */
    public OffsetDateTime timestamp() {
        return timestamp;
    }

    /**
     * 创建错误响应（工厂方法）
     *
     * <p><b>作用：</b>创建一个新的错误响应实例</p>
     * <p><b>参数：</b>
     * <ul>
     *   <li>code：错误代码</li>
     *   <li>message：错误消息</li>
     *   <li>requestId：请求ID</li>
     *   <li>path：请求路径</li>
     * </ul>
     * </p>
     * <p><b>特点：</b>
     * <ul>
     *   <li>success 自动设为 false</li>
     *   <li>timestamp 自动设为当前时间</li>
     *   <li>确保响应结构的一致性</li>
     * </ul>
     * </p>
     *
     * @param code 错误代码
     * @param message 错误消息
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 创建的错误响应
     */
    public static ApiErrorResponse of(ErrorCode code, String message, String requestId, String path) {
        return new ApiErrorResponse(false, code, message, requestId, path, OffsetDateTime.now());
    }

    /**
     * 快速创建客户端错误响应
     *
     * <p><b>作用：</b>创建客户端错误（4xx）的响应</p>
     * <p><b>示例：</b>
     * <pre>
     * ApiErrorResponse.badRequest(
     *     "deviceId 不能为空",
     *     request.getRequestId(),
     *     request.getRequestURI()
     * );
     * </pre>
     * </p>
     *
     * @param message 错误消息
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 错误响应
     */
    public static ApiErrorResponse badRequest(String message, String requestId, String path) {
        return of(ErrorCode.BAD_REQUEST, message, requestId, path);
    }

    /**
     * 快速创建未授权错误响应
     *
     * <p><b>作用：</b>创建未授权错误（401）的响应</p>
     *
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 错误响应
     */
    public static ApiErrorResponse unauthorized(String requestId, String path) {
        return of(ErrorCode.UNAUTHORIZED, "未授权访问，请先登录", requestId, path);
    }

    /**
     * 快速创建权限不足错误响应
     *
     * <p><b>作用：</b>创建权限不足错误（403）的响应</p>
     *
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 错误响应
     */
    public static ApiErrorResponse forbidden(String requestId, String path) {
        return of(ErrorCode.FORBIDDEN, "权限不足，无法访问", requestId, path);
    }

    /**
     * 快速创建资源不存在错误响应
     *
     * <p><b>作用：</b>创建资源不存在错误（404）的响应</p>
     *
     * @param resourceId 资源ID
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 错误响应
     */
    public static ApiErrorResponse notFound(String resourceId, String requestId, String path) {
        return of(ErrorCode.NOT_FOUND, String.format("资源 %s 不存在", resourceId), requestId, path);
    }

    /**
     * 快速创建服务端错误响应
     *
     * <p><b>作用：</b>创建服务端错误（5xx）的响应</p>
     *
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 错误响应
     */
    public static ApiErrorResponse internalServerError(String requestId, String path) {
        return of(ErrorCode.INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试", requestId, path);
    }

    /**
     * 快速创建业务错误响应
     *
     * <p><b>作用：</b>创建业务错误的响应</p>
     * <p><b>示例：</b>
     * <pre>
     * ApiErrorResponse.businessError(
     *     ErrorCode.EVALUATION_FAILED,
     *     "评估服务暂时不可用",
     *     requestId,
     *     path
     * );
     * </pre>
     * </p>
     *
     * @param errorCode 业务错误代码
     * @param message 错误消息
     * @param requestId 请求ID
     * @param path 请求路径
     * @return 错误响应
     */
    public static ApiErrorResponse businessError(ErrorCode errorCode, String message, String requestId, String path) {
        return of(errorCode, message, requestId, path);
    }

    /**
     * 获取错误代码的显示名称
     *
     * <p><b>作用：</b>获取错误代码的友好显示名称</p>
     *
     * @return 显示名称
     */
    public String getErrorName() {
        switch (code) {
            case BAD_REQUEST:
                return "请求参数错误";
            case UNAUTHORIZED:
                return "未授权访问";
            case FORBIDDEN:
                return "权限不足";
            case NOT_FOUND:
                return "资源不存在";
            case INTERNAL_SERVER_ERROR:
                return "服务器内部错误";
            case TOO_MANY_REQUESTS:
                return "请求过于频繁";
            default:
                return "未知错误";
        }
    }

    /**
     * 获取错误建议的解决方法
     *
     * <p><b>作用：</b>根据错误类型提供解决建议</p>
     *
     * @return 解决建议
     */
    public String getResolution() {
        switch (code) {
            case BAD_REQUEST:
                return "请检查请求参数是否正确，参考 API 文档";
            case UNAUTHORIZED:
                return "请检查登录状态，确保 token 有效";
            case FORBIDDEN:
                return "请确认您有足够的权限访问该资源";
            case NOT_FOUND:
                return "请确认请求的资源是否存在或路径是否正确";
            case INTERNAL_SERVER_ERROR:
                return "服务暂时不可用，请稍后重试。如持续出现问题，请联系客服";
            case TOO_MANY_REQUESTS:
                return "请求过于频繁，请稍后重试";
            default:
                return "请检查网络连接，如问题持续存在请联系客服";
        }
    }

    /**
     * 获取错误详情的日志信息
     *
     * <p><b>作用：</b>生成包含完整信息的错误日志</p>
     *
     * @return 日志信息
     */
    public String toLog() {
        return String.format("[%s] %s - %s | RequestID: %s | Path: %s | Timestamp: %s",
                code,
                getErrorName(),
                message,
                requestId,
                path,
                timestamp);
    }

    /**
     * 检查是否为可重试错误
     *
     * <p><b>作用：</b>判断错误是否适合重试</p>
     * <p><b>返回：</b>
     * <ul>
     *   <li>true：网络错误、服务临时不可用等</li>
     *   <li>false：参数错误、权限错误、资源不存在等</li>
     * </ul>
     * </p>
     *
     * @return 是否可重试
     */
    public boolean isRetryable() {
        switch (code) {
            case INTERNAL_SERVER_ERROR:
            case TOO_MANY_REQUESTS:
            case BAD_GATEWAY:
            case SERVICE_UNAVAILABLE:
            case GATEWAY_TIMEOUT:
                return true;
            default:
                return false;
        }
    }

    /**
     * 检查是否需要用户干预
     *
     * <p><b>作用：</b>判断错误是否需要用户采取行动</p>
     * <p><b>返回：</b>
     * <ul>
     *   <li>true：需要重新登录、修改参数等</li>
     *   <li>false：系统错误，等待恢复即可</li>
     * </ul>
     * </p>
     *
     * @return 是否需要用户干预
     */
    public boolean requiresUserAction() {
        switch (code) {
            case UNAUTHORIZED:
            case FORBIDDEN:
            case BAD_REQUEST:
            case NOT_FOUND:
                return true;
            default:
                return false;
        }
    }

    /**
     * 检查是否为严重错误
     *
     * <p><b>作用：</b>判断错误是否影响核心功能</p>
     * <p><b>返回：</b>
     * <ul>
     *   <li>true：影响核心业务功能</li>
     *   <li>false：非核心功能或临时问题</li>
     * </ul>
     * </p>
     *
     * @return 是否为严重错误
     */
    public boolean isSevere() {
        switch (code) {
            case INTERNAL_SERVER_ERROR:
            case SERVICE_UNAVAILABLE:
            case EVALUATION_FAILED:
            case PAYMENT_ERROR:
                return true;
            default:
                return false;
        }
    }
}