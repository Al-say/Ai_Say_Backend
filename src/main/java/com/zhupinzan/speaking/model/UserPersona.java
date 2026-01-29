package com.zhupinzan.speaking.model;

/**
 * 用户角色画像枚举，定义系统中支持的不同用户类型及其对应的行为特征。
 *
 * <h3>核心设计理念</h3>
 * <p>该枚举系统通过定义不同的用户画像，实现个性化学习和评估的精准匹配。
 * 每个画像不仅标识用户类型，更重要的是定义了该类型用户的学习风格、
 * 能力特征、评估偏好和沟通模式，为系统提供定制化的服务依据。</p>
 *
 * <h3>画像选择标准</h3>
 * <p>用户画像的选择基于以下关键维度：
 * <ul>
 *   <li><b>学习目标</b>：用户的主要学习目的和期望达成的成果</li>
 *   <li><b>能力水平</b>：用户当前的语言掌握程度和基础认知能力</li>
 *   <li><b>使用场景</b>：用户在实际环境中的语言应用场景和频率</li>
 *   <li><b>时间投入</b>：用户愿意投入的学习时间和每日练习频率</li>
 *   <li><b>反馈偏好</b>：用户对评估结果和反馈方式的需求</li>
 * </ul>
 * </p>
 *
 * <h3>评估标准差异</h3>
 * <p>不同画像的用户在评估时有明确的差异化标准：
 * <ul>
 *   <li><b>EXAM_PREP</b>：严格遵循考试评分标准，注重准确性和规范性</li>
 *   <li><b>CAREER_GROWTH</b>：侧重实用性表达和职场沟通效能</li>
 * </ul>
 * </p>
 *
 * <h3>与其他组件的协作关系</h3>
 * <p>该枚举与多个系统组件深度协作：
 * <ul>
 *   <li><b>SceneService</b>：根据画像推荐合适的练习场景</li>
 *   <li><b>AssessmentService</b>：应用对应的评估标准进行评分</li>
 *   <li><b>SceneRepository</b>：筛选适配特定画像的场景内容</li>
 *   <li><b>DailyTopicService</b>：生成符合画像特征的话题</li>
 *   <li><b>ProgressService</b>：基于画像特征跟踪学习进展</li>
 * </ul>
 * </p>
 *
 * <p>每个枚举值都代表了系统对用户群体的深刻理解，通过画像的精准定义，
 * 系统能够提供更加个性化的学习体验，提高用户的学习效率和满意度。</p>
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public enum UserPersona {
    /**
     * 备考党用户画像 (Exam Preparation)
     *
     * <h3>用户群体特征</h3>
     * <p><b>角色定位</b>：Exam Candidate - 考试备考者</p>
     * <p><b>风格特征</b>：Strict & Academic - 严格学术型</p>
     * <p><b>典型代表</b>：
     * <ul>
     *   <li>雅思、托福、四六级等标准化考试备考者</li>
     *   <li>国内考研英语、商务英语等专项考试考生</li>
     *   <li>准备语言能力证书的求职者</li>
     * </ul>
     * </p>
     *
     * <h3>学习需求特征</h3>
     * <p><b>核心诉求</b>：
     * <ul>
     *   <li>应对标准化考试，取得理想成绩</li>
     *   <li>掌握考试技巧和应试策略</li>
     *   <li>熟悉考试题型和评分标准</li>
     *   <li>限时训练，提高答题效率</li>
     * </ul>
     * </p>
     * <p><b>学习特点</b>：
     * <ul>
     *   <li>目标明确，学习动力强</li>
     *   <li>关注分数和排名，有竞争意识</li>
     *   <li>偏好系统性、结构化学习内容</li>
     *   <li>重视错题分析和弱点突破</li>
     * </ul>
     * </p>
     *
     * <h3>评估标准差异</h3>
     * <p><b>评估重点</b>：
     * <ul>
     *   <li><b>准确性优先</b>：语法、词汇、发音的准确性是首要标准</li>
     *   <li><b>规范性强</b>：表达方式要符合考试评分标准，避免非正式用语</li>
     *   <li><b>完整性要求</b>：回答要完整覆盖题目要求的要点</li>
     *   <li><b>时间压力</b>：在限定时间内完成表达，速度也是评估维度</li>
     * </ul>
     * </p>
     * <p><b>评分特点</b>：
     * <ul>
     *   <li>采用标准化评分量表（如雅思的1-9分制）</li>
     *   <li>侧重语法和词汇的正确使用</li>
     *   <li>强调回答的逻辑结构和条理性</li>
     *   <li>对错误零容忍，扣分明确</li>
     * </ul>
     * </p>
     *
     * <h3>适用场景类型</h3>
     * <p><b>场景特征</b>：
     * <ul>
     *   <li>考试模拟题和真题练习</li>
     *   <li>口语考试常见话题</li>
     *   <li>学术讨论和问答场景</li>
     *   <li>限时演讲和陈述</li>
     * </ul>
     * </p>
     *
     * <h3>与其他组件的协作</h3>
     * <p><b>场景适配</b>：
     * <ul>
     *   <li>SceneRepository会优先返回符合考试标准的场景</li>
     *   <li>初始Prompt会包含考试指导和要求说明</li>
     *   <li>场景描述会更加正式和学术化</li>
     * </ul>
     * </p>
     * <p><b>评估适配</b>：
     * <ul>
     *   <li>AssessmentService会使用考试专用评估模型</li>
     *   <li>DeepSeekEvalService会针对考试评分要求进行优化</li>
     *   <li>评估结果会提供详细的扣分点和改进建议</li>
     * </ul>
     * </p>
     * <p><b>内容适配</b>：
     * <ul>
     *   <li>PromptFactory生成考试导向的提示语</li>
     *   <li>初始Prompt包含考试技巧指导</li>
     *   <li>推荐问题集中在考试常见类型</li>
     * </ul>
     * </p>
     *
     * <h3>用户画像扩展性</h3>
     * <p>该画像可进一步细分，如：
     * <ul>
     *   <li><b>ACADEMIC_EXAM</b>：学术类考试（雅思、托福）</li>
     *   <li><b>PROFESSIONAL_EXAM</b>：职业类考试（商务英语、托业）</li>
     *   <li><b>GRADE_EXAM</b>：学业类考试（四六级、考研）</li>
     * </ul>
     * </p>
     */
    EXAM_PREP,

    /**
     * 职场发展用户画像 (Career Growth)
     *
     * <h3>用户群体特征</h3>
     * <p><b>角色定位</b>：Business Professional - 商务专业人士</p>
     * <p><b>风格特征</b>：Pragmatic & Concise - 务实简洁型</p>
     * <p><b>典型代表</b>：
     * <ul>
     *   <li>职场人士：商务人士、管理者、创业者</li>
     *   <li>职场新人：刚进入职场需要提升沟通能力的年轻人</li>
     *   <li>跨文化交流者：需要与国际同事或客户交流的员工</li>
     *   <li>求职者：准备面试和职场沟通的求职者</li>
     * </ul>
     * </p>
     *
     * <h3>学习需求特征</h3>
     * <p><b>核心诉求</b>：
     * <ul>
     *   <li>提升职场沟通效率和表达能力</li>
     *   <li>掌握商务谈判和演讲技巧</li>
     *   <li>增强职场人际关系处理能力</li>
     *   <li>适应国际化的工作环境</li>
     * </ul>
     * </p>
     * <p><b>学习特点</b>：
     * <ul>
     *   <li>注重实用性和可操作性</li>
     *   <li>偏好真实职场案例和情境</li>
     *   <li>希望快速见效，关注立竿见影的效果</li>
     *   <li>学习时间碎片化，需要灵活性</li>
     * </ul>
     * </p>
     *
     * <h3>评估标准差异</h3>
     * <p><b>评估重点</b>：
     * <ul>
     *   <li><b>沟通效能</b>：信息传递的准确性和说服力</li>
     *   <li><b>表达简洁</b>：简洁明了，避免冗余和废话</li>
     *   <li><b>职业性</b>：语言表达符合职业规范和礼仪</li>
     *   <li><b>应变能力</b>：对突发情况的应对和处理能力</li>
     * </ul>
     * </p>
     * <p><b>评分特点</b>：
     * <ul>
     *   <li>采用实用性评分标准，关注实际沟通效果</li>
     *   <li>注重表达的简洁性和专业性</li>
     *   <li>强调逻辑清晰和重点突出</li>
     *   <li>鼓励创新思维和独特见解</li>
     * </ul>
     * </p>
     *
     * <h3>适用场景类型</h3>
     * <p><b>场景特征</b>：
     * <ul>
     *   <li>会议和演讲场景</li>
     *   <li>商务谈判和协商</li>
     *   <li>客户服务和销售沟通</li>
     *   <li>团队管理和领导力表达</li>
     *   <li>职场社交和关系建立</li>
     * </ul>
     * </p>
     *
     * <h3>与其他组件的协作</h3>
     * <p><b>场景适配</b>：
     * <ul>
     *   <li>SceneRepository会返回职场相关的真实场景</li>
     *   <li>初始Prompt会包含商务背景和目标</li>
     *   <li>场景描述会体现职场需求和挑战</li>
     * </ul>
     * </p>
     * <p><b>评估适配</b>：
     * <ul>
     *   <li>AssessmentService会使用商务沟通评估模型</li>
     *   <li>DeepSeekEvalService会关注商务表达的专业性</li>
     *   <li>评估结果会提供职场沟通建议</li>
     * </ul>
     * </p>
     * <p><b>内容适配</b>：
     * <ul>
     *   <li>PromptFactory生成商务导向的提示语</li>
     *   <li>初始Prompt包含商务目标和情境</li>
     *   <li>推荐问题集中在商务沟通技巧</li>
     * </ul>
     * </p>
     *
     * <h3>用户画像扩展性</h3>
     * <p>该画像可进一步细分，如：
     * <ul>
     *   <li><b>MANAGERIAL</b>：管理者领导力沟通</li>
     *   <li><b>SALES</b>：销售和客户沟通</li>
     *   <li><b>TECHNICAL</b>：技术人员专业沟通</li>
     *   <li><b>CROSS_CULTURE</b>：跨文化商务沟通</li>
     * </ul>
     * </p>
     */
    CAREER_GROWTH;

    /**
     * 获取用户画像的显示名称
     *
     * <p>提供用户友好的画像名称，用于前端显示和用户界面。</p>
     *
     * @return 画像的中文显示名称
     */
    public String getDisplayName() {
        return switch (this) {
            case EXAM_PREP -> "备考党";
            case CAREER_GROWTH -> "职场发展";
        };
    }

    /**
     * 获取画像的学习目标类型
     *
     * <p>描述该画像用户的主要学习目标，用于个性化推荐系统。</p>
     *
     * @return 学习目标描述
     */
    public String getLearningObjective() {
        return switch (this) {
            case EXAM_PREP -> "通过标准化考试，取得理想成绩";
            case CAREER_GROWTH -> "提升职场沟通效能和职业竞争力";
        };
    }

    /**
     * 获取画像的难度等级
     *
     * <p>用于场景难度匹配和学习路径规划，数值越大难度越高。</p>
     *
     * @return 难度等级（1-5）
     */
    public int getDifficultyLevel() {
        return switch (this) {
            case EXAM_PREP -> 2;
            case CAREER_GROWTH -> 3;
        };
    }

    /**
     * 判断是否适合作为新用户的默认画像
     *
     * <p>用于新用户注册时的画像推荐，选择最适合新手的画像。</p>
     *
     * @return true 如果适合新手，false 否则
     */
    public boolean isSuitableForBeginner() {
        return switch (this) {
            case EXAM_PREP -> true;
            case CAREER_GROWTH -> false;
        };
    }

    /**
     * 获取画像的学习风格特征
     *
     * <p>用于个性化学习内容推荐和交互设计。</p>
     *
     * @return 学习风格描述
     */
    public String getLearningStyle() {
        return switch (this) {
            case EXAM_PREP -> "系统学习，注重练习，反复训练";
            case CAREER_GROWTH -> "实用导向，情境模拟，即时反馈";
        };
    }

    /**
     * 获取推荐的学习频率（每周）
     *
     * <p>基于画像特征，给出合理的练习建议。</p>
     *
     * @return 推荐每周练习次数
     */
    public int getRecommendedWeeklyFrequency() {
        return switch (this) {
            case EXAM_PREP -> 5;
            case CAREER_GROWTH -> 3;
        };
    }

    /**
     * 获取期望的学习效果
     *
     * <p>用于设置用户期望和学习目标管理。</p>
     *
     * @return 学习效果描述
     */
    public String getExpectedOutcome() {
        return switch (this) {
            case EXAM_PREP -> "考试成绩提升1-2个等级";
            case CAREER_GROWTH -> "职场沟通效率显著改善";
        };
    }
}
