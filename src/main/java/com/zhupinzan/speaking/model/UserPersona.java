package com.zhupinzan.speaking.model;

public enum UserPersona {
    EXAM_PREP("Exam Candidate", "Strict & Academic"),
    CAREER_GROWTH("Business Professional", "Pragmatic & Concise");

    private final String role;
    private final String style;

    UserPersona(String role, String style) {
        this.role = role;
        this.style = style;
    }

    public String getRole() {
        return role;
    }

    public String getStyle() {
        return style;
    }
}
/**
 * 用户角色画像枚举，定义系统中支持的不同用户类型及其对应的行为特征。
 *
 * <h3>设计理念</h3>
 * <p>该枚举采用角色-风格的二维设计模式，不仅标识用户的身份类型，
 * 还定义了每种用户期望的交互风格。这种设计使得AI助手能够根据用户画像
 * 调整回应方式和内容重点，提供更个性化的用户体验。</p>
 *
 * <h3>核心作用</h3>
 * <ul>
 *   <li>定义系统支持的用户画像类型</li>
 *   <li>指导AI助手的回应风格和内容重点</li>
 *   <li>为不同用户群体提供定制化的学习体验</li>
 *   <li>作为用户分类和个性化推荐的基础</li>
 * </ul>
 *
 * <h3>枚举值说明</h3>
 * <ul>
 *   <li><b>EXAM_PREP</b>: 备考党用户画像
 *     <ul>
 *       <li><b>角色</b>: Exam Candidate - 考试备考者</li>
 *       <li><b>风格</b>: Strict & Academic - 严格学术型</li>
 *       <li><b>特征描述</b>: 目标明确，注重考试技巧和得分，需要权威、准确的指导</li>
 *       <li><b>适用人群</b>: 雅思、托福、GRE等标准化考试的考生</li>
 *       <li><b>AI交互特点</b>: 提供结构化学习计划，强化考试技巧，给出明确评分标准</li>
 *     </ul>
 *   </li>
 *   <li><b>CAREER_GROWTH</b>: 职场发展用户画像
 *     <ul>
 *       <li><b>角色</b>: Business Professional - 商务专业人士</li>
 *       <li><b>风格</b>: Pragmatic & Concise - 务实简洁型</li>
 *       <li><b>特征描述</b>: 注重实用性，追求效率，需要直接、可用的建议</li>
 *       <li><b>适用人群</b>: 商务人士、职场新人、需要提升职场英语能力的人群</li>
 *       <li><b>AI交互特点</b>: 提供职场场景对话，注重实用表达，给出直接的建议和反馈</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>用户注册时的角色选择</li>
 *   <li>AI助手的回应风格调整</li>
 *   <li>学习内容的个性化推荐</li>
 *   <li>评估标准的差异化应用</li>
 *   <li>用户行为分析和统计</li>
 *   <li>数据库用户画像字段存储</li>
 * </ul>
 *
 * <h3>最佳实践</h3>
 * <ul>
 *   <li>根据用户画像调整AI提示词和回应策略</li>
 *   <li>在数据库设计中使用枚举值确保数据一致性</li>
 *   <li>为每种画像设计专门的评估标准</li>
 *   <li>允许用户在不同画像间切换，但保留历史记录</li>
 *   <li>基于画像数据优化产品功能和用户体验</li>
 * </ul>
 *
 * <h3>与其他组件的协作</h3>
 * <ul>
 *   <li>与AI服务：根据画像调整prompt和回应风格</li>
 *   <li>与评估服务：应用不同画像的评分标准</li>
 *   <li>与用户实体：存储用户选择的画像类型</li>
 *   <li>与内容推荐：基于画像推荐相关学习材料</li>
 *   <li>与前端界面：根据画像调整UI展示和交互方式</li>
 * </ul>
 *
 * <h3>设计考虑</h3>
 * <ul>
 *   <li>使用角色和风格两个维度描述用户画像</li>
 *   <li>通过final字段确保枚举值的不可变性</li>
 *   <li>使用中英文双语命名，便于国际化</li>
 *   <li>设计为可扩展，便于添加新的用户画像</li>
 *   <li>每种画像都有明确的业务场景和目标用户</li>
 * </ul>
 *
 * <h3>示例用法：</h3>
 * <pre>
 * {@code
 * // 数据库实体中使用
 * @Entity
 * public class User {
 *     @Enumerated(EnumType.STRING)
 *     private UserPersona persona; // 用户画像
 * }
 *
 * // AI服务中使用
 * @Service
 * public class AIGenerationService {
 *     public String generatePrompt(String content, UserPersona persona) {
 *         switch (persona) {
 *             case EXAM_PREP:
 *                 return String.format("你是一位专业的雅思考官，请从以下角度评估考生回答：%s", content);
 *             case CAREER_GROWTH:
 *                 return String.format("你是一位商务英语专家，请评估以下商务表达：%s", content);
 *             default:
 *                 return String.format("请评估以下内容：%s", content);
 *         }
 *     }
 * }
 *
 * // 评估服务中使用
 * @Service
 * public class AssessmentService {
 *     public AssessmentResult assess(String content, UserPersona persona) {
 *         switch (persona) {
 *             case EXAM_PREP:
 *                 // 使用学术标准评估
 *                 return academicAssessmentAssess(content);
 *             case CAREER_GROWTH:
 *                 // 使用商务标准评估
 *                 return businessAssessmentAssess(content);
 *             default:
 *                 return defaultAssessmentAssess(content);
 *         }
 *     }
 * }
 * }
 * </pre>
 *
 * <h3>扩展性考虑</h3>
 * <p>如果需要支持新的用户画像，可以扩展枚举：
 * <pre>
 * public enum UserPersona {
 *     EXAM_PREP("Exam Candidate", "Strict & Academic"),
 *     CAREER_GROWTH("Business Professional", "Pragmatic & Concise"),
 *     LANGUAGE_LEARNER("Language Learner", "Patient & Encouraging"),
 *     CONVERSATIONAL_PRACTICE("Conversational Practice", "Friendly & Casual");
 * }
 * </pre>
 * 新增的画像类型可以针对特定学习场景（如日常对话、旅游英语等）。</p>
 *
 * <h3>未来规划</h3>
 * <p>可以考虑增加更多维度来丰富用户画像：
 * <ul>
 *   <li>英语水平等级（Beginner, Intermediate, Advanced）</li>
 *   <li>学习偏好（Visual, Auditory, Kinesthetic）</li>
 *   <li>学习目标（Speaking, Writing, Reading, Listening）</li>
 *   <li>时间投入强度（Casual, Regular, Intensive）</li>
 * </ul>
 * </p>
 *
 * @see com.zhupinzan.speaking.service.AIGenerationService
 * @see com.zhupinzan.speaking.service.assessment.AssessmentService
 */