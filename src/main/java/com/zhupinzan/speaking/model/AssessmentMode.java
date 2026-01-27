package com.zhupinzan.speaking.model;

/**
 * 评估模式枚举，定义系统支持的不同评估类型。
 *
 * <h3>设计理念</h3>
 * <p>该枚举采用简单的二元设计，明确区分两种评估方式。
 * 通过枚举类型而非字符串常量，提供了更好的类型安全性和代码可读性，
 * 同时避免了硬编码字符串可能带来的拼写错误和一致性问题。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>定义系统支持的所有评估模式类型</li>
 *   <li>为评估流程提供类型安全的标识</li>
 *   <li>便于扩展新的评估方式</li>
 *   <li>作为数据库字段和API参数的有效值约束</li>
 * </ul>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li><b>AUDIO</b>: 音频评估模式
 *     <ul>
 *       <li>用户以语音形式输入回答</li>
 *       <li>系统通过音频识别和AI分析进行评估</li>
 *       <li>重点关注发音、流利度、语调等语音特征</li>
 *       <li>适用场景：口语练习、发音训练、即兴演讲评估</li>
 *     </ul>
 *   </li>
 *   <li><b>TEXT</b>: 文本评估模式
 *     <ul>
 *       <li>用户以文字形式输入回答</li>
 *       <li>系统通过文本分析和AI理解进行评估</li>
 *       <li>重点关注语法、词汇、逻辑、完整性等文本特征</li>
 *       <li>适用场景：写作练习、语法检查、逻辑思维训练</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>评估接口的输入参数类型</li>
 *   <li>数据库表字段定义（如assessment表的mode字段）</li>
 *   <li>业务逻辑中的条件判断和分支处理</li>
 *   <li>前端UI的评估模式选择</li>
 *   <li>API文档中的参数约束说明</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>使用枚举值而非字符串字面量</li>
 *   <li>在数据库设计中使用对应的数字或字符串映射</li>
 *   <li>在序列化/反序列化时保持一致性</li>
 *   <li>为每个枚举值添加有意义的注释说明</li>
 *   <li>考虑添加valueOf方法处理字符串转换</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与评估服务：根据模式调用不同的评估逻辑</li>
 *   <li>与数据库实体：作为字段类型的约束</li>
 *   <li>与前端API：定义支持的评估类型</li>
 *   <li>与AI服务：决定使用音频还是文本分析模型</li>
 *   <li>与用户界面：影响评估界面的展示方式</li>
 * </ul>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>保持枚举简单，不包含复杂逻辑</li>
 *   <li>使用大命名约定符合Java规范</li>
 *   <li>设计为可扩展，便于添加新的评估模式</li>
 *   <li>每个值都有明确的业务含义</li>
 *   <li>避免枚举值的组合使用（非互斥场景）</li>
 * </ul>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // 数据库实体中使用
 * @Entity
 * public class Assessment {
 *     @Enumerated(EnumType.STRING)
 *     private AssessmentMode mode; // AUDIO or TEXT
 * }
 *
 * // 业务逻辑中使用
 * @Service
 * public class AssessmentService {
 *     public AssessmentResult assess(String content, AssessmentMode mode) {
 *         switch (mode) {
 *             case AUDIO:
 *                 return audioAssessmentService.assess(content);
 *             case TEXT:
 *                 return textAssessmentService.assess(content);
 *             default:
 *                 throw new IllegalArgumentException("Unsupported assessment mode");
 *         }
 *     }
 * }
 *
 * // API接口中使用
 * @RestController
 * @RequestMapping("/api/assessment")
 * public class AssessmentController {
 *     @PostMapping
 *     public ResponseEntity<AssessmentResult> assess(
 *             @RequestParam AssessmentMode mode,
 *             @RequestParam String content) {
 *         // 根据模式进行评估
 *         return ResponseEntity.ok(assessmentService.assess(content, mode));
 *     }
 * }
 * }
 * </pre>
 *
 * <h3>扩展性考虑</h3>
 * <p>如果需要支持新的评估模式，只需在此枚举中添加新的值：
 * <pre>
 * public enum AssessmentMode {
 *     AUDIO, TEXT, VIDEO, IMAGE, CONVERSATION
 * }
 * </pre>
 * 系统中所有使用此枚举的地方都会自动获得新的模式支持。</p>
 *
 * @see com.zhupinzan.speaking.service.assessment.AudioAssessmentService
 * @see com.zhupinzan.speaking.service.assessment.TextAssessmentService
 */
public enum AssessmentMode {
    AUDIO, TEXT
}