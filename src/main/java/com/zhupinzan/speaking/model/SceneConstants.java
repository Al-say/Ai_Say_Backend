package com.zhupinzan.speaking.model;

import com.zhupinzan.speaking.model.UserPersona;

/**
 * 场景常量类，定义系统中所有场景类型的常量配置。
 *
 * <h3>设计原则</h3>
 * <p>本常量类采用以下设计原则：
 * <ul>
 *   <li><b>集中管理</b>：所有场景相关的常量集中定义，避免硬编码分散</li>
 *   <li><b>类型安全</b>：使用枚举和常量，确保类型安全</li>
 *   <li><b>易于扩展</b>：新增场景只需在此类中添加配置，无需修改多处代码</li>
 *   <li><b>文档完整</b>：每个常量都有详细的文档说明其用途和使用场景</li>
 *   <li><b>配置驱动</b>：场景特征通过常量配置，便于调整和优化</li>
 * </ul>
 * </p>
 *
 * <h3>常量分类</h3>
 * <p>常量按功能分为以下几类：
 * <ul>
 *   <li><b>场景分类常量</b>：定义场景的主要类别（日常生活、工作职场等）</li>
 *   <li><b>场景代码常量</b>：每个场景的唯一标识符，用于程序化引用</li>
 *   <li><b>场景描述常量</b>：场景的详细配置信息，包括标题、描述等</li>
 *   <li><b>难度等级常量</b>：定义场景的难度级别</li>
 *   <li><b>目标画像常量</b>：场景适用的用户画像类型</li>
 * </ul>
 * </p>
 *
 * <h3>使用场景</h3>
 * <p>本常量类广泛应用于以下场景：
 * <ul>
 *   <li><b>场景初始化</b>：系统启动时加载场景配置</li>
 *   <li><b>场景推荐</b>：根据用户画像匹配合适场景</li>
 *   <li><b>数据迁移</b>：数据库场景数据的批量操作</li>
 *   <li><b>界面显示</b>：前端展示场景列表和详情</li>
 *   <li><b>统计分析</b>：按分类统计场景使用情况</li>
 * </ul>
 * </p>
 *
 * <h3>与系统的协作关系</h3>
 * <p>本常量类与多个系统组件紧密协作：
 * <ul>
 *   <li><b>SceneService</b>：通过常量快速定位场景配置</li>
 *   <li><b>SceneRepository</b>：使用场景代码进行数据查询</li>
 *   <li><b>SceneDTO</b>：从常量生成响应数据对象</li>
 *   <li><b>AssessmentService</b>：根据场景类型选择评估策略</li>
 *   <li><b>DailyTopicService</b>：基于场景生成相关话题</li>
 * </ul>
 * </p>
 *
 * <h3>维护指南</h3>
 * <p>在添加新场景时的注意事项：
 * <ul>
 *   <li><b>代码命名</b>：使用有意义的英文单词，用下划线分隔</li>
 *   <li><b>唯一性</b>：确保每个场景代码唯一，避免冲突</li>
 *   <li><b>完整性</b>：新场景必须包含完整的配置信息</li>
 *   <li><b>分类合理</b>：将场景分配到合适的分类下</li>
 *   <li><b>难度匹配</b>：难度等级要与场景内容匹配</li>
 *   <li><b>画像适配</b>：选择合适的用户画像支持</li>
 * </ul>
 * </p>
 *
 * <h3>国际化考虑</h3>
 * <p>本常量类支持国际化扩展：
 * <ul>
 *   <li>所有显示文本应从常量中获取，避免硬编码</li>
 *   <li>可考虑创建资源文件管理多语言版本</li>
 *   <li>场景分类支持多种语言标签</li>
 *   <li>难度等级可本地化显示</li>
 * </ul>
 * </p>
 *
 * @author System Generated
 * @version 1.0
 * @since 1.0
 */
public final class SceneConstants {

    // 防止实例化
    private SceneConstants() {}

    // ========== 场景分类常量 ==========

    /**
     * 场景分类：日常生活
     *
     * <p>定义日常生活中常见的交流场景，包括购物、餐饮、交通等。
     * 这类场景贴近用户日常生活，使用频率高，是语言学习的基础内容。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>词汇简单实用，易于掌握</li>
     *   <li>情境直观，容易理解</li>
     *   <li>交流频率高，实用性强</li>
     *   <li>适合初学者入门练习</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>餐厅点餐</li>
     *   <li>购物询问</li>
     *   <li>问路指路</li>
     *   <li>天气闲聊</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_DAILY = "daily";

    /**
     * 场景分类：工作职场
     *
     * <p>定义职场中的专业交流场景，包括面试、会议、沟通等。
     * 这类场景注重专业性和效率，是职场人士必备的沟通技能。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>词汇专业规范，符合职场要求</li>
     *   <li>表达简洁明了，注重效率</li>
     *   <li>强调逻辑性和条理性</li>
     *   <li>需要一定的专业背景知识</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>工作面试</li>
     *   <li>团队会议</li>
     *   <li>汇报工作</li>
     *   <li>客户沟通</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_WORK = "work";

    /**
     * 场景分类：旅行出游
     *
     * <p>定义旅行相关的交流场景，包括酒店入住、景点游览、问路等。
     * 这类场景在旅行过程中频繁出现，需要应急沟通能力。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>词汇集中在旅游领域</li>
     *   <li>需要处理突发情况</li>
     *   <li>跨文化交流需求强烈</li>
     *   <li>时间压力较大</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>酒店入住</li>
     *   <li>景点咨询</li>
     *   <li>餐厅点餐</li>
     *   <li>交通问询</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_TRAVEL = "travel";

    /**
     * 场景分类：社交场合
     *
     * <p>定义社交活动中的交流场景，包括聚会、交友、闲聊等。
     * 这类场景注重建立人际关系，需要社交技巧和灵活性。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>语言灵活多样，可适当口语化</li>
     *   <li>注重情感表达和共鸣</li>
     *   <li>需要察言观色和应变能力</li>
     *   <li>话题广泛，知识面要求高</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>朋友聚会</li>
     *   <li>社交活动</li>
     *   <li>搭讪交流</li>
     *   <li>深度闲聊</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_SOCIAL = "social";

    /**
     * 场景分类：学习教育
     *
     * <p>定义教育学习相关的交流场景，包括课堂讨论、学术交流等。
     * 这类场景注重知识的准确性和逻辑性。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>词汇学术性强，准确性要求高</li>
     *   <li>表达逻辑严密，条理清晰</li>
     *   <li>需要专业知识支撑</li>
     *   <li>注重论证和说服能力</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>课堂发言</li>
     *   <li>小组讨论</li>
     *   <li>学术演讲</li>
     *   <li>课题汇报</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_EDUCATION = "education";

    /**
     * 场景分类：商务活动
     *
     * <p>定义商务谈判、演讲、接待等高端商务场景。
     * 这类场景要求极高的专业素养和沟通技巧。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>词汇正式专业，符合商务礼仪</li>
     *   <li>表达精准有力，富有说服力</li>
     *   <li>注重策略性和谈判技巧</li>
     *   <li>需要高水平的应变能力</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>商务谈判</li>
     *   <li>产品推介</li>
     *   <li>客户接待</li>
     *   <li>商业演讲</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_BUSINESS = "business";

    /**
     * 场景分类：紧急情况
     *
     * <p>定义紧急情况下的求助和应急处理场景。
     * 这类场景要求快速反应和有效沟通。</p>
     *
     * <p><b>适用特征</b>：
     * <ul>
     *   <li>表达简短明确，突出重点</li>
     *   <li>需要快速理解问题核心</li>
     *   <li>时间紧迫，压力大</li>
     *   <li>需要清晰的指令和说明</li>
     * </ul>
     * </p>
     *
     * <p><b>典型场景</b>：
     * <ul>
     *   <li>医疗求助</li>
     *   <li>报警处理</li>
     *   <li>突发事故</li>
     *   <li>紧急避险</li>
     * </ul>
     * </p>
     */
    public static final String CATEGORY_EMERGENCY = "emergency";

    /**
     * 获取场景分类的显示名称
     *
     * <p>根据场景分类代码返回用户友好的显示名称。</p>
     *
     * @param category 场景分类代码
     * @return 显示名称，如果分类不存在则返回"其他"
     */
    public static String getCategoryDisplayName(String category) {
        if (category == null) {
            return "未分类";
        }

        return switch (category) {
            case CATEGORY_DAILY -> "日常生活";
            case CATEGORY_WORK -> "工作职场";
            case CATEGORY_TRAVEL -> "旅行出游";
            case CATEGORY_SOCIAL -> "社交场合";
            case CATEGORY_EDUCATION -> "学习教育";
            case CATEGORY_BUSINESS -> "商务活动";
            case CATEGORY_EMERGENCY -> "紧急情况";
            default -> "其他";
        };
    }

    // ========== 场景代码常量 ==========

    /**
     * 场景代码：餐厅点餐
     *
     * <p>场景描述：在餐厅中向服务员点餐的对话练习。
     * 学习重点：菜名表达、喜好说明、价格询问、特殊要求。</p>
     *
     * <p><b>适用画像</b>：EXAM_PREP, CAREER_GROWTH
     * <p><b>难度等级</b>：1
     * <p><b>推荐时长</b>：3-5分钟
     * <p><b>关键词</b>：餐厅，菜单，点餐，菜品，口味
     */
    public static final String SCENE_RESTAURANT_ORDERING = "restaurant_ordering";

    /**
     * 场景代码：工作面试
     *
     * <p>场景描述：职场面试中的自我介绍和回答问题的练习。
     * 学习重点：自我介绍、工作经验、技能展示、职业规划。</p>
     *
     * <p><b>适用画像</b>：CAREER_GROWTH
     * <p><b>难度等级</b>：3
     * <p><b>推荐时长</b>：10-15分钟
     * <p><b>关键词</b>：面试，求职，职业，技能，经验
     */
    public static final String SCENE_JOB_INTERVIEW = "job_interview";

    /**
     * 场景代码：酒店入住
     *
     * <p>场景描述：在酒店前台办理入住手续的对话练习。
     * 学习重点：身份验证、房型选择、服务需求、费用确认。</p>
     *
     * <p><b>适用画像</b>：EXAM_PREP, CAREER_GROWTH
     * <p><b>难度等级</b>：2
     * <p><b>推荐时长</b>：5-8分钟
     * <p><b>关键词</b>：酒店，入住，房间，预订，证件
     */
    public static final String SCENE_HOTEL_CHECKIN = "hotel_checkin";

    /**
     * 场景代码：天气闲聊
     *
     * <p>场景描述：关于天气的日常闲聊对话练习。
     * 学习重点：天气描述、感受表达、话题延伸、社交润滑。</p>
     *
     * <p><b>适用画像</b>：EXAM_PREP
     * <p><b>难度等级</b>：1
     * <p><b>推荐时长</b>：3-5分钟
     * <p><b>关键词</b>：天气，温度，晴朗，下雨，舒适度
     */
    public static final String SCENE_SMALL_TALK_WEATHER = "small_talk_weather";

    /**
     * 场景代码：商务谈判
     *
     * <p>场景描述：商务合作中的谈判和协商对话练习。
     * 学习重点：利益诉求、让步策略、协议达成、关系维护。</p>
     *
     * <p><b>适用画像</b>：CAREER_GROWTH
     * <p><b>难度等级</b>：5
     * <p><b>推荐时长</b>：15-20分钟
     * <p><b>关键词</b>：谈判，合作，协议，利益，双赢
     */
    public static final String SCENE_BUSINESS_NEGOTIATION = "business_negotiation";

    // ========== 场景配置集合 ==========

    /**
     * 所有场景代码集合
     *
     * <p>提供系统中所有场景代码的快速访问接口，
     * 用于验证场景代码的有效性或批量处理所有场景。</p>
     */
    public static final String[] ALL_SCENE_CODES = {
        SCENE_RESTAURANT_ORDERING,
        SCENE_JOB_INTERVIEW,
        SCENE_HOTEL_CHECKIN,
        SCENE_SMALL_TALK_WEATHER,
        SCENE_BUSINESS_NEGOTIATION
    };

    // ========== 场景配置映射 ==========

    /**
     * 场景配置映射
     *
     * <p>定义每个场景的详细配置信息，包括：
     * - 场景标题
     * - 场景描述
     * - 适用分类
     * - 目标画像
     * - 难度等级
     * - 推荐时长
     * - 关键词
     * - 初始提示模板
     * - 推荐问题列表</p>
     *
     * <p>使用场景：
     * <ul>
     *   <li>初始化场景数据</li>
     *   <li>生成场景DTO</li>
     *   <li>场景推荐算法</li>
     *   <li>场景难度分析</li>
     * </ul>
     * </p>
     */
    public static class SceneConfig {
        private final String code;
        private final String title;
        private final String description;
        private final String category;
        private final UserPersona[] targetPersonas;
        private final int difficultyLevel;
        private final int recommendedDurationMinutes;
        private final String[] keywords;
        private final String initialPromptTemplate;
        private final String[] recommendedPrompts;

        /**
         * 构造函数
         *
         * @param code 场景代码
         * @param title 场景标题
         * @param description 场景描述
         * @param category 场景分类
         * @param targetPersonas 目标用户画像
         * @param difficultyLevel 难度等级
         * @param recommendedDurationMinutes 推荐时长（分钟）
         * @param keywords 关键词数组
         * @param initialPromptTemplate 初始提示模板
         * @param recommendedPrompts 推荐问题数组
         */
        public SceneConfig(String code, String title, String description, String category,
                          UserPersona[] targetPersonas, int difficultyLevel,
                          int recommendedDurationMinutes, String[] keywords,
                          String initialPromptTemplate, String[] recommendedPrompts) {
            this.code = code;
            this.title = title;
            this.description = description;
            this.category = category;
            this.targetPersonas = targetPersonas;
            this.difficultyLevel = difficultyLevel;
            this.recommendedDurationMinutes = recommendedDurationMinutes;
            this.keywords = keywords;
            this.initialPromptTemplate = initialPromptTemplate;
            this.recommendedPrompts = recommendedPrompts;
        }

        // Getters
        public String getCode() { return code; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public UserPersona[] getTargetPersonas() { return targetPersonas; }
        public int getDifficultyLevel() { return difficultyLevel; }
        public int getRecommendedDurationMinutes() { return recommendedDurationMinutes; }
        public String[] getKeywords() { return keywords; }
        public String getInitialPromptTemplate() { return initialPromptTemplate; }
        public String[] getRecommendedPrompts() { return recommendedPrompts; }

        /**
         * 检查场景是否支持指定用户画像
         *
         * @param persona 用户画像
         * @return true 如果支持，false 否则
         */
        public boolean supportsPersona(UserPersona persona) {
            if (targetPersonas == null || targetPersonas.length == 0) {
                return true; // 通用场景支持所有画像
            }

            for (UserPersona p : targetPersonas) {
                if (p == persona) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 获取场景显示名称
         *
         * @return "[分类] 标题" 格式的显示名称
         */
        public String getDisplayName() {
            return String.format("[%s] %s", getCategoryDisplayName(category), title);
        }
    }

    /**
     * 获取场景配置映射
     *
     * <p>返回所有场景的配置信息映射，
     * 场景代码作为键，配置对象作为值。</p>
     *
     * @return 场景配置映射
     */
    public static java.util.Map<String, SceneConfig> getSceneConfigMap() {
        java.util.Map<String, SceneConfig> configMap = new java.util.HashMap<>();

        // 餐厅点餐场景配置
        configMap.put(SCENE_RESTAURANT_ORDERING, new SceneConfig(
            SCENE_RESTAURANT_ORDERING,
            "餐厅点餐",
            "在餐厅中向服务员点餐的对话练习，包括浏览菜单、询问菜品、表达喜好、确认订单等环节。",
            CATEGORY_DAILY,
            new UserPersona[]{UserPersona.EXAM_PREP, UserPersona.CAREER_GROWTH},
            1,
            5,
            new String[]{"餐厅", "菜单", "点餐", "菜品", "口味"},
            "你好，请给我推荐几道特色菜。",
            new String[]{
                "今天的招牌菜是什么？",
                "这个菜辣吗？",
                "我想要一个不辣的菜品",
                "能介绍下菜品的做法吗？"
            }
        ));

        // 工作面试场景配置
        configMap.put(SCENE_JOB_INTERVIEW, new SceneConfig(
            SCENE_JOB_INTERVIEW,
            "工作面试",
            "职场面试中的自我介绍和回答问题的练习，包括个人背景、工作经验、技能展示、职业规划等方面。",
            CATEGORY_WORK,
            new UserPersona[]{UserPersona.CAREER_GROWTH},
            3,
            15,
            new String[]{"面试", "求职", "职业", "技能", "经验"},
            "请简单介绍一下你自己。",
            new String[]{
                "你的职业规划是什么？",
                "为什么选择我们公司？",
                "你的优势和缺点是什么？",
                "你对这个职位有什么期望？"
            }
        ));

        // 酒店入住场景配置
        configMap.put(SCENE_HOTEL_CHECKIN, new SceneConfig(
            SCENE_HOTEL_CHECKIN,
            "酒店入住",
            "在酒店前台办理入住手续的对话练习，包括身份验证、房型选择、服务需求、费用确认等环节。",
            CATEGORY_TRAVEL,
            new UserPersona[]{UserPersona.EXAM_PREP, UserPersona.CAREER_GROWTH},
            2,
            8,
            new String[]{"酒店", "入住", "房间", "预订", "证件"},
            "你好，我预订了房间。",
            new String[]{
                "有安静一些的房间吗？",
                "房间包含早餐吗？",
                "Wi-Fi密码是什么？",
                "退房时间是几点？"
            }
        ));

        // 天气闲聊场景配置
        configMap.put(SCENE_SMALL_TALK_WEATHER, new SceneConfig(
            SCENE_SMALL_TALK_WEATHER,
            "天气闲聊",
            "关于天气的日常闲聊对话练习，包括天气描述、感受表达、话题延伸等社交润滑技巧。",
            CATEGORY_SOCIAL,
            new UserPersona[]{UserPersona.EXAM_PREP},
            1,
            5,
            new String[]{"天气", "温度", "晴朗", "下雨", "舒适度"},
            "今天天气真不错啊！",
            new String[]{
                "你喜欢什么样的天气？",
                "这种天气适合做什么？",
                "天气对你的心情有什么影响？",
                "记得带伞，可能会下雨。"
            }
        ));

        // 商务谈判场景配置
        configMap.put(SCENE_BUSINESS_NEGOTIATION, new SceneConfig(
            SCENE_BUSINESS_NEGOTIATION,
            "商务谈判",
            "商务合作中的谈判和协商对话练习，包括利益诉求、让步策略、协议达成、关系维护等高级沟通技巧。",
            CATEGORY_BUSINESS,
            new UserPersona[]{UserPersona.CAREER_GROWTH},
            5,
            20,
            new String[]{"谈判", "合作", "协议", "利益", "双赢"},
            "关于合作方案，我有些想法想和您沟通。",
            new String[]{
                "您认为这个方案的可行性如何？",
                "在哪些方面我们可以让步？",
                "如何实现双赢的局面？",
                "协议的主要条款有哪些？"
            }
        ));

        return java.util.Collections.unmodifiableMap(configMap);
    }

    /**
     * 根据场景代码获取场景配置
     *
     * @param sceneCode 场景代码
     * @return 场景配置，如果不存在则返回null
     */
    public static SceneConfig getSceneConfig(String sceneCode) {
        return getSceneConfigMap().get(sceneCode);
    }

    /**
     * 根据用户画像获取场景配置列表
     *
     * @param persona 用户画像
     * @return 适用于该画像的场景配置列表
     */
    public static java.util.List<SceneConfig> getSceneConfigsForPersona(UserPersona persona) {
        return getSceneConfigMap().values().stream()
            .filter(config -> config.supportsPersona(persona))
            .sorted((a, b) -> Integer.compare(a.getDifficultyLevel(), b.getDifficultyLevel()))
            .toList();
    }

    /**
     * 根据分类获取场景配置列表
     *
     * @param category 场景分类
     * @return 属于该分类的场景配置列表
     */
    public static java.util.List<SceneConfig> getSceneConfigsByCategory(String category) {
        return getSceneConfigMap().values().stream()
            .filter(config -> category.equals(config.getCategory()))
            .sorted((a, b) -> a.getTitle().compareTo(b.getTitle()))
            .toList();
    }

    // ========== 场景难度常量 ==========

    /**
     * 场景难度：入门级
     *
     * <p>特征：
     * <ul>
     *   <li>词汇简单基础</li>
     *   <li>句子结构简单</li>
     *   <li>情境直观明了</li>
     *   <li>交流流程固定</li>
     * </ul>
     * </p>
     */
    public static final int DIFFICULTY_BEGINNER = 1;

    /**
     * 场景难度：基础级
     *
     * <p>特征：
     * <ul>
     *   <li>词汇量中等</li>
     *   <li>需要一定的表达技巧</li>
     *   <li>情境稍有复杂</li>
     *   <li>需要一定的应变能力</li>
     * </ul>
     * </p>
     */
    public static final int DIFFICULTY_BASIC = 2;

    /**
     * 场景难度：中级
     *
     * <p>特征：
     * <ul>
     *   <li>词汇较为丰富</li>
     *   <li>需要灵活表达</li>
     *   <li>情境较为复杂</li>
     *   <li>需要较强的沟通能力</li>
     * </ul>
     * </p>
     */
    public static final int DIFFICULTY_INTERMEDIATE = 3;

    /**
     * 场景难度：中高级
     *
     * <p>特征：
     * <ul>
     *   <li>词汇专业性强</li>
     *   <li>表达需要精准</li>
     *   <li>情境高度复杂</li>
     *   <li>需要专业的沟通技巧</li>
     * </ul>
     * </p>
     */
    public static final int DIFFICULTITY_ADVANCED = 4;

    /**
     * 场景难度：专家级
     *
     * <p>特征：
     * <ul>
     *   <li>词汇高度专业化</li>
     *   <li>表达需要艺术性</li>
     *   <li>情境高度抽象或专业</li>
     *   <li>需要大师级的沟通能力</li>
     * </ul>
     * </p>
     */
    public static final int DIFFICULTITY_EXPERT = 5;

    /**
     * 根据难度等级获取显示名称
     *
     * @param difficulty 难度等级
     * @return 难度显示名称
     */
    public static String getDifficultyDisplayName(int difficulty) {
        return switch (difficulty) {
            case DIFFICULTY_BEGINNER -> "入门级";
            case DIFFICULTY_BASIC -> "基础级";
            case DIFFICULTY_INTERMEDIATE -> "中级";
            case DIFFICULTITY_ADVANCED -> "中高级";
            case DIFFICULTITY_EXPERT -> "专家级";
            default -> "未知";
        };
    }
}