package com.zhupinzan.speaking.model.dto;

import com.zhupinzan.speaking.model.UserPersona;

import java.util.List;

/**
 * 场景数据传输对象
 *
 * <p><b>作用：</b>封装对话场景的完整信息，包括基础信息、目标用户、对话引导和相关资源。
 * 用于在系统内传递和存储对话场景相关的数据，支持多样化的口语练习场景。</p>
 *
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>使用 Java Record 确保数据不可变性</li>
 *   <li>支持多维度场景分类</li>
 *   <li>针对不同用户角色定制化</li>
 *   <li>灵活的初始提示和推荐问题</li>
 *   <li>支持多媒体资源（图片）</li>
 * </ul>
 *
 * <p><b>业务场景：</b></p>
 * <ul>
 *   <li>口语练习场景选择和展示</li>
 *   <li>基于用户水平的个性化推荐</li>
 *   <li>对话流程引导和初始化</li>
 *   <li>场景分类和搜索</li>
 *   <li>场景完成度和进度跟踪</li>
 * </ul>
 *
 * <p><b>数据关系：</b></p>
 * <ul>
 *   <li>id：数据库主键，唯一标识</li>
 *   <li>code：场景编码，业务标识</li>
 *   <li>targetPersona：目标用户角色，决定难度和内容</li>
 *   <li>initialPrompt：对话起始点</li>
 *   <li>recommendedPrompts：备选问题列表</li>
 *   <li>imageUrl：场景视觉辅助</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>Long 类型的 id 序列化为 JSON Number</li>
 *   <li>UserPersona 枚举使用字符串值</li>
 *   <li>List&lt;String&gt; 保持数组格式</li>
 *   <li>imageUrl 支持 HTTPS URL 格式</li>
 * </ul>
 *
 * <p><b>验证规则：</b></p>
 * <ul>
 *   <li>id：正整数，非空</li>
 *   <li>code：非空，唯一，字母数字组合</li>
 *   <li>title：非空，长度 1-100 字符</li>
 *   <li>description：非空，长度 1-2000 字符</li>
 *   <li>category：非空，预定义分类</li>
 *   <li>targetPersona：非空，有效的 UserPersona</li>
 *   <li>initialPrompt：非空，长度 1-1000 字符</li>
 *   <li>imageUrl：可选，有效的 HTTPS URL</li>
 *   <li>recommendedPrompts：字符串数组，每个长度 1-200 字符</li>
 * </ul>
 *
 * <p><b>前端交互规范：</b></p>
 * <ul>
 *   <li>场景列表：卡片式展示，包含标题、描述、分类</li>
 *   <li>场景详情：展开显示完整信息，包括推荐问题</li>
 *   <li>场景选择：基于用户角色推荐适配场景</li>
 *   <li>图片加载：异步加载，考虑加载状态</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public record SceneDTO(
        Long id,
        String code,
        String title,
        String description,
        String category,
        UserPersona targetPersona,
        String initialPrompt,
        String imageUrl,
        List<String> recommendedPrompts
) {

    /**
     * 场景ID
     *
     * <p><b>作用：</b>数据库主键，唯一标识一个场景</p>
     * <p><b>数据类型：</b>Long</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>正整数 (> 0)</li>
     *   <li>数据库自增 ID</li>
     *   <li>唯一标识，不可重复</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>数据库表主键</li>
     *   <li>API 路由参数</li>
     *   <li>前端状态标识</li>
     *   <li>与其他数据表关联</li>
     * </ul>
     * </p>
     */
    public Long id() {
        return id;
    }

    /**
     * 场景编码
     *
     * <p><b>作用：</b>业务层面的场景标识符，用于程序化引用</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>格式规范：</b>
     * <ul>
     *   <li>使用字母数字组合</li>
     *   <li>小写字母开头</li>
     *   <li>用下划线分隔单词</li>
     *   <li>长度 3-50 字符</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <ul>
     *   <li>restaurant_ordering</li>
     *   <li>job_interview</li>
     *   <li>travel_checkin</li>
     *   <li>small_talk_weather</li>
     * </ul>
     * </p>
     * <p><b>设计原则：</b>
     * <ul>
     *   <li>保持唯一性</li>
     *   <li>易于理解和记忆</li>
     *   <li>支持程序化处理</li>
     *   <li>版本兼容性</li>
     * </ul>
     * </p>
     */
    public String code() {
        return code;
    }

    /**
     * 场景标题
     *
     * <p><b>作用：</b>场景的简短描述和显示名称</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度 1-100 字符</li>
     *   <li>简洁明了，吸引人</li>
     *   <li>体现场景核心内容</li>
     * </ul>
     * </p>
     * <p><b>设计要求：</b>
     * <ul>
     *   <li>长度适中，便于列表展示</li>
     *   <li>使用积极正面的语言</li>
     *   <li>避免技术术语，使用日常用语</li>
     *   <li>考虑跨文化理解</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <ul>
     *   <li>餐厅点餐</li>
     *   <li>工作面试</li>
     *   <li>酒店入住</li>
     *   <li>天气闲聊</li>
     * </ul>
     * </p>
     */
    public String title() {
        return title;
    }

    /**
     * 场景描述
     *
     * <p><b>作用：</b>场景的详细说明和背景介绍</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度 1-2000 字符</li>
     *   <li>内容详实具体</li>
     *   <li>包含情境和目标</li>
     * </ul>
     * </p>
     * <p><b>内容结构：</b>
     * <ul>
     *   <li><b>背景介绍</b>：场景发生的时间、地点、人物</li>
     *   <li><b>目标描述</b>：练习的目标和期望</li>
     *   <li><b>情境说明</b>：具体的对话情境</li>
     *   <li><b>角色分配</b>：说话双方的身份和关系</li>
     * </ul>
     * </p>
     * <p><b>写作技巧：</b>
     * <ul>
     *   <li>使用情景化描述</li>
     *   <li>突出实用性</li>
     *   <li>保持语言简洁</li>
     *   <li>提供足够的上下文</li>
     * </ul>
     * </p>
     */
    public String description() {
        return description;
    }

    /**
     * 场景分类
     *
     * <p><b>作用：</b>对场景进行分类管理，便于组织和筛选</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>预定义分类：</b>
     * <ul>
     *   <li><b>daily</b>：日常生活（购物、餐饮、交通）</li>
     *   <li><b>work</b>：工作职场（面试、会议、沟通）</li>
     *   <li><b>travel</b>：旅行出游（酒店、景点、问路）</li>
     *   <li><b>social</b>：社交场合（聚会、交友、闲聊）</li>
     *   <li><b>education</b>：学习教育（课堂、讨论、演讲）</li>
     *   <li><b>business</b>：商务活动（谈判、演讲、接待）</li>
     *   <li><b>emergency</b>：紧急情况（求助、报警、医疗）</li>
     * </ul>
     * </p>
     * <p><b>分类作用：</b>
     * <ul>
     *   <li>场景组织和导航</li>
     *   <li>个性化推荐依据</li>
     *   <li>学习进度跟踪</li>
     *   <li>难度分级管理</li>
     * </ul>
     * </p>
     */
    public String category() {
        return category;
    }

    /**
     * 目标用户角色
     *
     * <p><b>作用：</b>定义场景适用的用户角色和难度级别</p>
     * <p><b>数据类型：</b>UserPersona 枚举</p>
     * <p><b>可选值：</b>
     * <ul>
     *   <li>BEGINNER：初学者，基础场景</li>
     *   <li>INTERMEDIATE：中级用户，中等复杂度</li>
     *   <li>ADVANCED：高级用户，复杂场景</li>
     *   <li>EXPERT：专家级，专业场景</li>
     * </ul>
     * </p>
     * <p><b>角色差异化：</b>
     * <ul>
     *   <li><b>初学者场景</b>：简单对话，常用词汇，明确流程</li>
     *   <li><b>中级场景</b>：适度复杂，需要应变，词汇丰富</li>
     *   <li><b>高级场景</b>：复杂情境，抽象概念，灵活表达</li>
     *   <li><b>专家场景</b>：专业领域，深度讨论，辩论谈判</li>
     * </ul>
     * </p>
     */
    public UserPersona targetPersona() {
        return targetPersona;
    }

    /**
     * 初始提示
     *
     * <p><b>作用：</b>对话开始的引导语或第一个问题</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度 1-1000 字符</li>
     *   <li>自然流畅，符合情境</li>
     *   <li>开放性而非封闭性</li>
     * </ul>
     * </p>
     * <p><b>设计原则：</b>
     * <ul>
     *   <li>符合角色身份</li>
     *   <li>引导对话展开</li>
     *   <li>包含关键信息</li>
     *   <li>避免过于具体</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <ul>
     *   <li>"你好，欢迎光临！请问有什么可以帮您的吗？"</li>
     *   <li>"今天天气真不错，您有什么计划吗？"</li>
     *   <li>"能简单介绍一下您的工作经验吗？"</li>
     * </ul>
     * </p>
     */
    public String initialPrompt() {
        return initialPrompt;
    }

    /**
     * 场景图片 URL
     *
     * <p><b>作用：</b>场景相关的视觉辅助材料</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>格式：</b>有效的 HTTPS URL</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>可选字段（可为 null）</li>
     *   <li>长度不超过 2048 字符</li>
     *   <li>必须使用 HTTPS 协议</li>
     *   <li>支持常见图片格式：JPG、PNG、GIF、WebP</li>
     * </ul>
     * </p>
     * <p><b>图片用途：</b>
     * <ul>
     *   <li>场景背景图</li>
     *   <li>情境示意图</li>
     *   <li>角色形象图</li>
     *   <li>物品展示图</li>
     * </ul>
     * </p>
     * <p><b>加载策略：</b>
     * <ul>
     *   <li>异步加载，不影响页面性能</li>
     *   <li>支持懒加载和预加载</li>
     *   <li>提供加载占位图</li>
     *   <li>考虑响应式适配</li>
     * </ul>
     * </p>
     */
    public String imageUrl() {
        return imageUrl;
    }

    /**
     * 推荐提示列表
     *
     * <p><b>作用：</b>提供对话过程中的备选问题和引导</p>
     * <p><b>数据类型：</b>List&lt;String&gt;</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>可选字段，可为空列表</li>
     *   <li>每个字符串长度 1-200 字符</li>
     *   <li>数量 0-10 个</li>
     *   <li>内容相关且多样</li>
     * </ul>
     * </p>
     * <p><b>设计原则：</b>
     * <ul>
     *   <li>开放性问题为主</li>
     *   <li>避免引导性过强</li>
     *   <li>覆盖不同角度</li>
     *   <li>难度适中</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <pre>
     * [
     *   "您能详细说说吗？",
     *   "这个经历对您有什么影响？",
     *   "您是怎么应对这个情况的？",
     *   "有什么想补充的吗？"
     * ]
     * </pre>
     * </p>
     */
    public List<String> recommendedPrompts() {
        return recommendedPrompts;
    }

    /**
     * 获取场景显示名称
     *
     * <p><b>作用：</b>获取场景的友好显示名称，包含分类信息</p>
     * <p><b>格式：</b>"[分类] 标题"</p>
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        return String.format("[%s] %s", getCategoryDisplayName(), title);
    }

    /**
     * 获取分类显示名称
     *
     * <p><b>作用：</b>获取分类的友好显示名称</p>
     *
     * @return 分类显示名称
     */
    public String getCategoryDisplayName() {
        switch (category) {
            case "daily":
                return "日常生活";
            case "work":
                return "工作职场";
            case "travel":
                return "旅行出游";
            case "social":
                return "社交场合";
            case "education":
                return "学习教育";
            case "business":
                return "商务活动";
            case "emergency":
                return "紧急情况";
            default:
                return "其他";
        }
    }

    /**
     * 获取角色显示名称
     *
     * <p><b>作用：</b>获取目标用户角色的友好显示名称</p>
     *
     * @return 角色显示名称
     */
    public String getPersonaDisplayName() {
        switch (targetPersona) {
            case BEGINNER:
                return "初学者";
            case INTERMEDIATE:
                return "中级";
            case ADVANCED:
                return "高级";
            case EXPERT:
                return "专家";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否包含推荐提示
     *
     * <p><b>作用：</b>判断场景是否提供备选问题</p>
     *
     * @return true 如果有推荐提示，false 否则
     */
    public boolean hasRecommendedPrompts() {
        return recommendedPrompts != null && !recommendedPrompts.isEmpty();
    }

    /**
     * 获取难度等级
     *
     * <p><b>作用：</b>根据目标用户角色映射难度等级</p>
     * <p><b>返回：</b>难度等级（1-5）</p>
     *
     * @return 难度等级
     */
    public int getDifficultyLevel() {
        switch (targetPersona) {
            case BEGINNER:
                return 1;
            case INTERMEDIATE:
                return 2;
            case ADVANCED:
                return 4;
            case EXPERT:
                return 5;
            default:
                return 3;
        }
    }

    /**
     * 获取推荐提示（限制数量）
     *
     * <p><b>作用：</b>获取指定数量的推荐提示</p>
     * <p><b>参数：</b>最大数量</p>
     *
     * @param maxCount 最大数量
     * @return 限制数量的推荐提示列表
     */
    public List<String> getRecommendedPrompts(int maxCount) {
        if (recommendedPrompts == null || recommendedPrompts.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        if (recommendedPrompts.size() <= maxCount) {
            return java.util.Collections.unmodifiableList(recommendedPrompts);
        }

        return recommendedPrompts.subList(0, maxCount);
    }

    /**
     * 创建场景（工厂方法）
     *
     * <p><b>作用：</b>创建一个新的场景实例</p>
     * <p><b>参数：</b>
     * <ul>
     *   <li>title：场景标题</li>
     *   <li>description：场景描述</li>
     *   <li>category：场景分类</li>
     *   <li>targetPersona：目标用户角色</li>
     *   <li>initialPrompt：初始提示</li>
     *   <li>recommendedPrompts：推荐提示列表</li>
     * </ul>
     * </p>
     *
     * @param title 标题
     * @param description 描述
     * @param category 分类
     * @param targetPersona 目标角色
     * @param initialPrompt 初始提示
     * @param recommendedPrompts 推荐提示
     * @return 创建的场景
     */
    public static SceneDTO create(
            String title,
            String description,
            String category,
            UserPersona targetPersona,
            String initialPrompt,
            List<String> recommendedPrompts
    ) {
        Long id = System.currentTimeMillis(); // 简单模拟 ID 生成
        String code = generateCode(title, category);

        return new SceneDTO(
            id,
            code,
            title,
            description,
            category,
            targetPersona,
            initialPrompt,
            null, // imageUrl 可以后续设置
            recommendedPrompts
        );
    }

    /**
     * 生成场景编码
     *
     * <p><b>作用：</b>基于标题和分类生成场景编码</p>
     *
     * @param title 标题
     * @param category 分类
     * @return 场景编码
     */
    private static String generateCode(String title, String category) {
        // 简化的编码生成逻辑
        String normalizedTitle = title.toLowerCase()
            .replaceAll("[\\s\\p{Punct}]+", "_")
            .replaceAll("^_|_$", "");

        String normalizedCategory = category.toLowerCase();

        return String.format("%s_%s", normalizedCategory, normalizedTitle);
    }

    /**
     * 获取场景标签
     *
     * <p><b>作用：</b>获取场景相关的标签列表</p>
     * <p><b>返回：</b>基于分类和角色的标签</p>
     *
     * @return 标签列表
     */
    public List<String> getTags() {
        List<String> tags = new java.util.ArrayList<>();
        tags.add(category);
        tags.add(targetPersona.name().toLowerCase());

        // 添加更多相关标签
        switch (category) {
            case "daily":
                tags.addAll(List.of("practical", "common"));
                break;
            case "work":
                tags.addAll(List.of("professional", "career"));
                break;
            case "travel":
                tags.addAll(List.of("tourism", "abroad"));
                break;
            case "social":
                tags.addAll(List.of("communication", "relationship"));
                break;
        }

        return java.util.Collections.unmodifiableList(tags);
    }

    /**
     * 检查场景难度匹配
     *
     * <p><b>作用：</b>检查当前用户角色是否适合此场景</p>
     * <p><b>参数：</b>当前用户角色</p>
     * <p><b>返回：</b>true 如果匹配，false 否则</p>
     *
     * @param currentPersona 当前用户角色
     * @return 是否匹配
     */
    public boolean isSuitableFor(UserPersona currentPersona) {
        // 简单匹配逻辑：当前角色应小于等于目标角色
        int currentLevel = currentPersona.ordinal();
        int targetLevel = targetPersona.ordinal();

        return currentLevel <= targetLevel;
    }
}