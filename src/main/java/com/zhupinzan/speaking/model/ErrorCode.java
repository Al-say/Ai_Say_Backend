package com.zhupinzan.speaking.model;

import lombok.Getter;

/**
 * 系统错误码枚举，定义系统中所有可能的错误类型及其对应的中文描述。
 *
 * <h3>设计理念</h3>
 * <p>该枚举采用统一错误码设计模式，为系统中的所有错误提供标准化的标识。
 * 通过错误码而非异常类来分类错误，使得错误处理更加灵活，并且便于前端进行错误类型的判断和用户友好的提示。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>提供标准化的错误标识系统</li>
 *   <li>支持前端错误分类和用户提示</li>
 *   <li>便于日志记录和错误追踪</li>
 *   <li>实现全局异常处理的一致性</li>
 *   <li>支持多语言错误提示（通过description字段）</li>
 * </ul>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li><b>BAD_REQUEST</b>: 请求参数错误
 *     <ul>
 *       <li>HTTP状态码：400</li>
 *       <li>触发场景：请求参数格式错误、缺失必填参数、参数类型不匹配</li>
 *       <li>处理方式：返回详细的参数错误信息</li>
 *     </ul>
 *   </li>
 *   <li><b>NOT_FOUND</b>: 资源不存在
 *     <ul>
 *       <li>HTTP状态码：404</li>
 *       <li>触发场景：请求的资源ID不存在、URL路径错误</li>
 *       <li>处理方式：提示资源不存在，建议检查请求参数</li>
 *     </ul>
 *   </li>
 *   <li><b>UNAUTHORIZED</b>: 未经授权
 *     <ul>
 *       <li>HTTP状态码：401</li>
 *       <li>触发场景：缺少认证token、token过期、token格式错误</li>
 *       <li>处理方式：引导用户重新登录</li>
 *     </ul>
 *   </li>
 *   <li><b>FORBIDDEN</b>: 禁止访问
 *     <ul>
 *       <li>HTTP状态码：403</li>
 *       <li>触发场景：权限不足、访问被禁止、账号被封禁</li>
 *       <li>处理方式：提示权限不足，联系管理员</li>
 *     </ul>
 *   </li>
 *   <li><b>VALIDATION_ERROR</b>: 数据校验失败
 *     <ul>
 *       <li>HTTP状态码：422</li>
 *       <li>触发场景：业务逻辑校验失败、数据格式约束违反</li>
 *       <li>处理方式：返回具体的校验失败字段和原因</li>
 *     </ul>
 *   </li>
 *   <li><b>CONFLICT</b>: 数据冲突/重复
 *     <ul>
 *       <li>HTTP状态码：409</li>
 *       <li>触发场景：重复注册、数据版本冲突、资源状态冲突</li>
 *       <li>处理方式：提示冲突原因，建议用户重新操作</li>
 *     </ul>
 *   </li>
 *   <li><b>INTERNAL_ERROR</b>: 服务器内部错误
 *     <ul>
 *       <li>HTTP状态码：500</li>
 *       <li>触发场景：未预期的异常、系统错误、第三方服务故障</li>
 *       <li>处理方式：记录详细日志，返回通用错误提示</li>
 *     </ul>
 *   </li>
 *   <li><b>AI_SERVICE_ERROR</b>: AI服务调用失败
 *     <ul>
 *       <li>HTTP状态码：502</li>
 *       <li>触发场景：DeepSeek API调用失败、AI服务不可用</li>
 *       <li>处理方式：重试机制，服务降级处理</li>
 *       <li>特殊说明：针对DeepSeek场景预留的错误码</li>
 *     </ul>
 *   </li>
 *   <li><b>STORAGE_ERROR</b>: 文件存储服务不可用
 *     <ul>
 *       <li>HTTP状态码：503</li>
 *       <li>触发场景：OSS/S3服务故障、存储空间不足、文件上传失败</li>
 *       <li>处理方式：切换备用存储，提示用户稍后重试</li>
 *     </ul>
 *   </li>
 *   <li><b>AUDIO_TRANSCODE_ERROR</b>: 音频转码失败
 *     <ul>
 *       <li>HTTP状态码：415</li>
 *       <li>触发场景：音频格式不支持、转码服务故障、文件损坏</li>
 *       <li>处理方式：提示支持的格式，建议重新上传</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>全局异常处理器中定义错误响应</li>
 *   <li>Service层抛出业务异常时指定错误码</li>
 *   <li>Controller层的参数校验失败</li>
 *   <li>第三方服务调用失败时的错误分类</li>
 *   <li>前端错误页面的显示和用户提示</li>
 *   <li>监控系统的错误统计和报警</li>
 *   <li>日志系统的错误分类和级别控制</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>在Controller层捕获异常并转换为对应的错误码</li>
 *   <li>使用全局异常处理器统一处理错误响应</li>
 *   <li>为每个错误码添加详细的中文描述</li>
 *   <li>保持错误码的原子性，一个错误码对应一种错误场景</li>
 *   <li>在日志中记录错误码和详细信息</li>
 *   <li>为前端提供错误码的映射和处理建议</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与全局异常处理器：将异常转换为标准错误响应</li>
 *   <li>与Controller：处理参数校验和业务异常</li>
 *   <li>与Service：抛出业务相关的异常</li>
 *   <li>与前端API：定义错误响应格式</li>
 *   <li>与监控系统：提供错误分类和统计</li>
 *   <li>与日志系统：支持错误级别的日志记录</li>
 * </ul>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>使用String类型提供更好的可读性和国际化支持</li>
 *   <li>使用final字段确保错误码的不可变性</li>
 *   <li>错误码命名遵循HTTP状态码的逻辑分组</li>
 *   <li>description字段支持多语言显示</li>
 *   <li>设计为可扩展，便于添加新的错误类型</li>
 *   <li>使用Lombok的@Getter简化代码</li>
 * </ul>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // 全局异常处理器中
 * @RestControllerAdvice
 * public class GlobalExceptionHandler {
 *     @ExceptionHandler(MethodArgumentNotValidException.class)
 *     public ResponseEntity<ErrorResponse> handleValidationException(
 *             MethodArgumentNotValidException ex) {
 *         // 将参数校验异常映射为VALIDATION_ERROR
 *         return ResponseEntity.badRequest()
 *                 .body(new ErrorResponse(ErrorCode.VALIDATION_ERROR,
 *                         ex.getBindingResult().getAllErrors().toString()));
 *     }
 * }
 *
 * // Service层中使用
 * @Service
 * public class UserService {
 *     public User createUser(UserDTO userDTO) {
 *         // 检查用户名是否重复
 *         if (userRepository.existsByUsername(userDTO.getUsername())) {
 *             throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
 *         }
 *         // 其他业务逻辑...
 *     }
 * }
 *
 * // Controller层中使用
 * @RestController
 * @RequestMapping("/api/users")
 * public class UserController {
 *     @PostMapping
 *     public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) {
 *         try {
 *             User user = userService.createUser(userDTO);
 *             return ResponseEntity.ok(user);
 *         } catch (BusinessException e) {
 *             return ResponseEntity.status(e.getErrorCode().getHttpStatus())
 *                     .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
 *         }
 *     }
 * }
 * }
 * </pre>
 *
 * <h3>错误响应格式</h3>
 * <p>建议的统一错误响应格式：
 * <pre>
 * {
 *   "code": "BAD_REQUEST",
 *   "message": "请求参数错误",
 *   "details": "字段名：错误原因",
 *   "timestamp": "2023-01-01T12:00:00Z"
 * }
 * </pre>
 * </p>
 *
 * <h3>扩展性考虑</h3>
 * <p>如果需要支持更多错误类型，可以扩展枚举：
 * <pre>
 * public enum ErrorCode {
 *     // 现有错误码...
 *     RATE_LIMIT_EXCEEDED("请求频率超限"),
 *     QUOTA_EXCEEDED("配额用尽"),
 *     PAYMENT_REQUIRED("需要付费"),
 *     FEATURE_NOT_AVAILABLE("功能不可用");
 * }
 * </pre>
 * 同时配合错误码版本管理，支持向后兼容。</p>
 *
 * <h3>国际化支持</h3>
 * <p>description字段可以支持多语言：
 * <ul>
 *   <li>中文环境：直接使用中文描述</li>
 *   <li>英文环境：可以创建ErrorCodeEn枚举或使用资源文件映射</li>
 *   <li>其他语言：根据需求扩展</li>
 * </ul>
 * </p>
 *
 * @see com.zhupinzan.speaking.config.GlobalExceptionHandler
 * @see com.zhupinzan.speaking.exception.BusinessException
 */
@Getter
public enum ErrorCode {
    BAD_REQUEST("请求参数错误"),
    NOT_FOUND("资源不存在"),
    UNAUTHORIZED("未经授权"),
    FORBIDDEN("禁止访问"),
    VALIDATION_ERROR("数据校验失败"),
    CONFLICT("数据冲突/重复"),
    INTERNAL_SERVER_ERROR("服务器内部错误"),
    TOO_MANY_REQUESTS("请求过于频繁"),
    BAD_GATEWAY("网关错误"),
    SERVICE_UNAVAILABLE("服务不可用"),
    GATEWAY_TIMEOUT("网关超时"),
    INTERNAL_ERROR("服务器内部错误"),
    AI_SERVICE_ERROR("AI 服务调用失败"), // 针对 DeepSeek 场景预留
    STORAGE_ERROR("文件存储服务不可用"),
    AUDIO_TRANSCODE_ERROR("音频转码失败"),
    EVALUATION_FAILED("评估失败"),
    PAYMENT_ERROR("支付错误");

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }
}