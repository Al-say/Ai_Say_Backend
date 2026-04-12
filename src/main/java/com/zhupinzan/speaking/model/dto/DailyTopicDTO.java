package com.zhupinzan.speaking.model.dto;

import com.zhupinzan.speaking.model.Level;
import com.zhupinzan.speaking.model.UserPersona;
import static com.zhupinzan.speaking.model.Level.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * 每日话题数据传输对象
 *
 * <p><b>作用：</b>封装每日话题的完整信息，包括日期、用户角色、话题内容、图片和扩展数据。
 * 用于在系统内传递和存储每日话题相关的数据。</p>
 *
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>使用 Java Record 确保数据不可变性</li>
 *   <li>包含日期信息用于话题管理</li>
 *   <li>支持用户角色定制化内容</li>
 *   <li>灵活的 payload 结构支持扩展</li>
 *   <li>图片资源支持多种格式</li>
 * </ul>
 *
 * <p><b>业务场景：</b></p>
 * <ul>
 *   <li>每日话题推送和展示</li>
 *   <li>基于用户角色的个性化内容</li>
 *   <li>话题完成进度跟踪</li>
 *   <li>话题相关的资源管理</li>
 *   <li>话题历史记录和查询</li>
 * </ul>
 *
 * <p><b>数据关系：</b></p>
 * <ul>
 *   <li>date：唯一标识一天的话题</li>
 *   <li>persona：决定话题的呈现方式和难度</li>
 *   <li>title：话题的简短描述</li>
 *   <li>prompt：话题的具体内容</li>
 *   <li>imageUrl：话题配图或示例图片</li>
 *   <li>payload：扩展数据，支持动态结构</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>LocalDate 自动序列化为 ISO-8601 格式</li>
 *   <li>UserPersona 枚举使用字符串值</li>
 *   <li>payload 使用 Map 支持动态键值对</li>
 *   <li>imageUrl 支持 HTTPS URL 格式</li>
 * </ul>
 *
 * <p><b>验证规则：</b></p>
 * <ul>
 *   <li>date：非空，有效的 LocalDate</li>
 *   <li>persona：非空，有效的 UserPersona 枚举值</li>
 *   <li>title：非空，长度 1-100 字符</li>
 *   <li>prompt：非空，长度 1-2000 字符</li>
 *   <li>imageUrl：可选，有效的 HTTPS URL</li>
 *   <li>payload：可选，JSON 兼容结构</li>
 * </ul>
 *
 * <p><b>前端交互规范：</b></p>
 * <ul>
 *   <li>日期格式：YYYY-MM-DD</li>
 *   <li>角色字段：直接使用枚举值</li>
 *   <li>图片加载：异步加载，支持缓存</li>
 *   <li>扩展数据：根据类型动态解析</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public record DailyTopicDTO(
    LocalDate date,
    UserPersona persona,
    String title,
    String prompt,
    String imageUrl,
    Map<String, Object> payload
) {

    /**
     * 日期
     *
     * <p><b>作用：</b>标识话题所属的日期</p>
     * <p><b>数据类型：</b>LocalDate</p>
     * <p><b>格式：</b>ISO-8601 日期格式（YYYY-MM-DD）</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>必须是有效的日期</li>
     *   <li>不能为未来日期（除非是预发布）</li>
     *   <li>用于话题的唯一标识和排序</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>话题的日期标识</li>
     *   <li>历史话题查询</li>
     *   <li>话题完成状态跟踪</li>
     *   <li>话题推送时间控制</li>
     * </ul>
     * </p>
     */
    public LocalDate date() {
        return date;
    }

    /**
     * 用户角色
     *
     * <p><b>作用：</b>定义话题的目标用户角色和难度级别</p>
     * <p><b>数据类型：</b>UserPersona 枚举</p>
     * <p><b>可选值：</b>
     * <ul>
     *   <li>BEGINNER：初学者，简单话题</li>
     *   <li>INTERMEDIATE：中级用户，中等难度</li>
     *   <li>ADVANCED：高级用户，复杂话题</li>
     *   <li>EXPERT：专家级，专业话题</li>
     * </ul>
     * </p>
     * <p><b>影响：</b>
     * <ul>
     *   <li>话题难度和复杂度</li>
     *   <li>词汇和语法要求</li>
     *   <li>话题时长和深度</li>
     *   <li>评分标准和期望</li>
     * </ul>
     * </p>
     */
    public UserPersona persona() {
        return persona;
    }

    /**
     * 话题标题
     *
     * <p><b>作用：</b>话题的简短描述和标题</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度 1-100 字符</li>
     *   <li>简洁明了，吸引人</li>
     *   <li>包含关键词便于搜索</li>
     * </ul>
     * </p>
     * <p><b>设计要求：</b>
     * <ul>
     *   <li>长度适中，避免过长</li>
     *   <li>使用积极的语言</li>
     *   <li>体现话题的核心内容</li>
     *   <li>考虑不同文化背景的接受度</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <ul>
     *   <li>初学者："我的日常生活"</li>
     *   <li>中级："我眼中的科技发展"</li>
     *   <li>高级："全球化对文化的影响"</li>
     * </ul>
     * </p>
     */
    public String title() {
        return title;
    }

    /**
     * 话题提示/问题
     *
     * <p><b>作用：</b>话题的具体内容、问题描述或提示信息</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度 1-2000 字符</li>
     *   <li>内容清晰具体</li>
     *   <li>引导性而非限制性</li>
     * </ul>
     * </p>
     * <p><b>内容结构：</b>
     * <ul>
     *   <li><b>背景介绍</b>：话题的背景信息</li>
     *   <li><b>具体问题</b>：需要回答的问题</li>
     *   <li><b>要求说明</b>：回答的具体要求</li>
     *   <li><b>示例提示</b>（可选）：回答方向的提示</li>
     * </ul>
     * </p>
     * <p><b>角色差异化：</b>
     * <ul>
     *   <li>初学者：简单直接，提供模板</li>
     *   <li>中级：有一定深度，需要分析</li>
     *   <li>高级：复杂多元，需要批判性思维</li>
     *   <li>专家：专业深入，需要专业知识</li>
     * </ul>
     * </p>
     */
    public String prompt() {
        return prompt;
    }

    /**
     * 图片 URL
     *
     * <p><b>作用：</b>话题相关的图片资源地址</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>格式：</b>有效的 HTTPS URL</p>
     * <p><p>约束条件：</b>
     * <ul>
     *   <li>可选字段（可为 null）</li>
     *   <li>长度不超过 2048 字符</li>
     *   <li>必须使用 HTTPS 协议</li>
     *   <li>支持常见图片格式：JPG、PNG、GIF、WebP</li>
     * </ul>
     * </p>
     * <p><b>图片用途：</b>
     * <ul>
     *   <li>话题背景图</li>
     *   <li>示例图片</li>
     *   <li>图表或示意图</li>
     *   <li>相关的视觉素材</li>
     * </ul>
     * </p>
     * <p><b>加载策略：</b>
     * <ul>
     *   <li>异步加载，不阻塞页面渲染</li>
     *   <li>支持图片懒加载</li>
     *   <li>提供加载失败回退方案</li>
     *   <li>考虑图片的适配性和响应式设计</li>
     * </ul>
     * </p>
     */
    public String imageUrl() {
        return imageUrl;
    }

    /**
     * 扩展数据载荷
     *
     * <p><b>作用：</b>存储话题的扩展信息和动态数据</p>
     * <p><b>数据类型：</b>Map&lt;String, Object&gt;</p>
     * <p><p>特点：</b>
     * <ul>
     *   <li>键值对结构，支持灵活扩展</li>
     *   <li>值类型可以是 String、Number、Boolean、Array、Object</li>
     *   <li>不影响核心字段的向后兼容性</li>
     *   <li>支持特殊功能和定制化需求</li>
     * </ul>
     * </p>
     * <p><b>常见用途：</b>
     * <ul>
     *   <li><b>话题标签</b>：["conversation", "daily", "beginner"]</li>
     *   <li><b>预计时长</b>：{"duration": "5-10", "unit": "minutes"}</li>
     *   <li><b>相关话题</b>：["topic_123", "topic_456"]</li>
     *   <li><b>难度系数</b>：{"level": 2, "max": 5}</li>
     *   <li><b>语音提示</b>：{"hasAudio": true, "audioUrl": "..."}</li>
     *   <li><b>评分权重</b>：{"fluency": 0.3, "completeness": 0.4, "relevance": 0.3}</li>
     * </ul>
     * </p>
     * <p><b>设计原则：</b>
     * <ul>
     *   <li>保持数据结构的清晰和可预测性</li>
     *   <li>避免过度使用，优先使用核心字段</li>
     *   <li>提供文档说明常见字段含义</li>
     *   <li>考虑前端兼容性和处理逻辑</li>
     * </ul>
     * </p>
     */
    public Map<String, Object> payload() {
        return payload;
    }

    /**
     * 获取话题标签
     *
     * <p><b>作用：</b>从 payload 中提取话题标签</p>
     * <p><b>返回：</b>标签列表，如果不存在则返回空列表</p>
     *
     * @return 话题标签列表
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getTags() {
        if (payload != null && payload.containsKey("tags")) {
            Object tags = payload.get("tags");
            if (tags instanceof java.util.List) {
                return (java.util.List<String>) tags;
            }
        }
        return java.util.Collections.emptyList();
    }

    /**
     * 获取预计时长
     *
     * <p><b>作用：</b>从 payload 中提取预计完成时间</p>
     * <p><b>返回：</b>时长字符串，格式如 "5-10 分钟"</p>
     *
     * @return 预计时长描述
     */
    public String getEstimatedDuration() {
        if (payload != null && payload.containsKey("duration")) {
            Object duration = payload.get("duration");
            if (duration instanceof String) {
                return (String) duration;
            }
        }
        return "5-10 分钟";
    }

    /**
     * 检查是否包含语音提示
     *
     * <p><b>作用：</b>判断话题是否提供语音辅助</p>
     *
     * @return true 如果有语音提示，false 否则
     */
    public boolean hasAudioHint() {
        if (payload != null && payload.containsKey("hasAudio")) {
            Object hasAudio = payload.get("hasAudio");
            return Boolean.TRUE.equals(hasAudio);
        }
        return false;
    }

    /**
     * 获取话题难度等级
     *
     * <p><b>作用：</b>从 payload 中获取难度等级</p>
     * <p><b>返回：</b>难度等级（1-5），默认为 3</p>
     *
     * @return 难度等级
     */
    public int getDifficultyLevel() {
        if (payload != null && payload.containsKey("level")) {
            Object level = payload.get("level");
            if (level instanceof Number) {
                return ((Number) level).intValue();
            }
        }
        return 3;
    }

    /**
     * 获取评分权重配置
     *
     * <p><b>作用：</b>获取不同评估维度的权重配置</p>
     * <p><b>返回：</b>权重配置 Map，如果不存在返回默认权重</p>
     *
     * @return 权重配置
     */
    public Map<String, Double> getScoreWeights() {
        if (payload != null && payload.containsKey("scoreWeights")) {
            Object weights = payload.get("scoreWeights");
            if (weights instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Double> result = (Map<String, Double>) weights;
                return result;
            }
        }
        // 默认权重
        return Map.of(
            "fluency", 0.3,
            "completeness", 0.4,
            "relevance", 0.3
        );
    }

    /**
     * 检查是否为新话题
     *
     * <p><b>作用：</b>判断话题是否是最近发布的</p>
     * <p><b>判断逻辑：</b>话题日期是今天或昨天</p>
     *
     * @return true 如果是新话题，false 否则
     */
    public boolean isNew() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        return date.equals(today) || date.equals(yesterday);
    }

    /**
     * 获取角色显示名称
     *
     * <p><b>作用：</b>获取用户角色的友好显示名称</p>
     *
     * @return 角色显示名称
     */
    public String getPersonaDisplayName() {
        switch (persona) {
            case EXAM_PREP:
                return "备考党";
            case CAREER_GROWTH:
                return "职场人";
            default:
                return "未知";
        }
    }

    /**
     * 创建每日话题（工厂方法）
     *
     * <p><b>作用：</b>创建一个新的每日话题实例</p>
     * <p><b>参数：</b>
     * <ul>
     *   <li>date：话题日期</li>
     *   <li>persona：用户角色</li>
     *   <li>title：话题标题</li>
     *   <li>prompt：话题内容</li>
     *   <li>imageUrl：图片地址（可选）</li>
     *   <li>tags：话题标签</li>
     * </ul>
     * </p>
     *
     * @param date 日期
     * @param persona 用户角色
     * @param title 标题
     * @param prompt 内容
     * @param imageUrl 图片地址
     * @param tags 标签列表
     * @return 创建的每日话题
     */
    public static DailyTopicDTO create(
        LocalDate date,
        UserPersona persona,
        String title,
        String prompt,
        String imageUrl,
        java.util.List<String> tags
    ) {
        Map<String, Object> payload = Map.of(
            "tags", tags,
            "createdAt", java.time.LocalDateTime.now(),
            "difficulty", getDifficultyByPersona(persona)
        );

        return new DailyTopicDTO(date, persona, title, prompt, imageUrl, payload);
    }

    /**
     * 根据用户角色获取默认难度
     *
     * <p><b>作用：</b>根据用户角色返回默认难度设置</p>
     *
     * @param persona 用户角色
     * @return 难度等级
     */
    private static int getDifficultyByPersona(UserPersona persona) {
        switch (persona) {
            case EXAM_PREP:
                return 3;
            case CAREER_GROWTH:
                return 3;
            default:
                return 3;
        }
    }
}