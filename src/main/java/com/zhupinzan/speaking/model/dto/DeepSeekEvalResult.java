package com.zhupinzan.speaking.model.dto;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek 评估结果对象
 *
 * <p><b>作用：</b>封装来自 DeepSeek AI 服务的详细评估结果，提供多维度分析和个性化反馈。
 * 这是一个强类型的 JSON Schema，确保与 DeepSeek API 的数据契约完全匹配。</p>
 *
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>强类型验证：使用 Java Record 确保数据结构正确</li>
 *   <li>嵌套结构：Feedback 和 Issue 内部结构化</li>
 *   <li>工厂方法：提供多种预设的降级结果</li>
 *   <li>异常处理：内置错误状态处理机制</li>
 *   <li>扩展性：支持多种评估维度</li>
 * </ul>
 *
 * <p><b>评估维度：</b></p>
 * <ul>
 *   <li><b>流利度 (Fluency)</b>：语音表达的流畅程度</li>
 *   <li><b>完整性 (Completeness)</b>：内容的完整程度</li>
 *   <li><b>相关性 (Relevance)</b>：回答的相关性</li>
 *   <li><b>发音 (Pronunciation)</b>：发音准确度</li>
 *   <li><b>语法 (Grammar)</b>：语法正确性</li>
 *   <li><b>词汇 (Vocabulary)</b>：词汇丰富度</li>
 * </ul>
 *
 * <p><b>状态管理：</b></p>
 * <ul>
 *   <li><b>success</b>：评估成功完成</li>
 *   <li><b>error</b>：评估过程中发生错误</li>
 *   <li><b>no_speech</b>：未检测到语音输入</li>
 *   <li><b>processing</b>：正在处理中（异步场景）</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>所有字段都会序列化为 JSON</li>
 *   <li>嵌套对象自动序列化</li>
 *   <li>集合类型保持数组格式</li>
 *   <li>数字类型保持原样（Integer 使用 JSON Number）</li>
 * </ul>
 *
 * <p><b>错误处理策略：</b></p>
 * <ul>
 *   <li>使用 fallback 方法创建降级结果</li>
 *   <li>确保数据结构完整性</li>
 *   <li>提供有意义的错误信息</li>
 *   <li>保证前端接收的数据格式一致</li>
 * </ul>
 *
 * <p><b>与系统集成的考虑：</b></p>
 * <ul>
 *   <li>作为 DeepSeek API 的响应 DTO</li>
 *   <li>与其他评估结果兼容</li>
 *   <li>支持缓存和持久化</li>
 *   <li>可被其他服务消费</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public record DeepSeekEvalResult(
    String status,
    Integer overallScore,
    Map<String, Integer> metrics,
    Feedback feedback
) {

    /**
     * 反馈详情对象
     *
     * <p><b>作用：</b>封装 AI 评估的详细反馈信息，包括总结、优点、问题和建议。</p>
     * <p><b>数据结构：</b>DeepSeekEvalResult 的嵌套结构</p>
     * <p><b>内容特点：</b>
     * <ul>
     *   <li>总结：整体表现的概括评价</li>
     *   <li>优点：突出的表现亮点</li>
     *   <li>问题：需要改进的具体问题</li>
     *   <li>建议：具体的改进方案</li>
     *   <li>改进版本：优化后的示例文本</li>
     * </ul>
     * </p>
     */
    public record Feedback(
        String summary,
        List<String> strengths,
        List<Issue> issues,
        List<String> suggestions,
        String improvedVersion
    ) {}

    /**
     * 问题详情对象
     *
     * <p><b>作用：</b>封装具体的评估问题，包含问题类型、证据和修复方案。</p>
     * <p><b>数据结构：</b>Feedback 的嵌套结构</p>
     * <p><b>字段说明：</b>
     * <ul>
     *   <li><b>type</b>：问题类型（如：语法、词汇、发音等）</li>
     *   <li><b>evidence</b>：问题的具体表现或证据</li>
     *   <li><b>fix</b>：修复建议或方案</li>
     * </ul>
     * </p>
     */
    public record Issue(String type, String evidence, String fix) {}

    /**
     * 创建错误状态的降级结果
     *
     * <p><b>作用：</b>当 DeepSeek API 调用失败时，创建一个结构完整的降级结果。</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>API 调用超时</li>
     *   <li>API 返回错误</li>
     *   <li>网络连接失败</li>
     *   <li>服务不可用</li>
     * </ul>
     * </p>
     * <p><b>特点：</b>
     * <ul>
     *   <li>设置状态为 "error"</li>
     *   <li>所有评分为 0</li>
     *   <li>提供错误原因</li>
     *   <li>给出通用的重试建议</li>
     * </ul>
     * </p>
     *
     * @param reason 错误原因描述
     * @return 降级的评估结果
     */
    public static DeepSeekEvalResult fallback(String reason) {
        return new DeepSeekEvalResult(
            "error",
            0,
            Map.of(
                "fluency", 0,
                "completeness", 0,
                "relevance", 0,
                "pronunciation", 0,
                "grammar", 0,
                "vocabulary", 0
            ),
            new Feedback(
                "Evaluation failed: " + reason,
                List.of(),
                List.of(),
                List.of("Try again later."),
                ""
            )
        );
    }

    /**
     * 更通用的 fallback 方法
     *
     * <p><b>作用：</b>创建自定义状态和消息的降级结果。</p>
     * <p><b>优势：</b>相比 fallback(String)，支持更灵活的状态控制。</p>
     * <p><b>参数：</b>
     * <ul>
     *   <li>status：自定义状态码</li>
     *   <li>msg：自定义错误消息</li>
     * </ul>
     * </p>
     *
     * @param status 状态码
     * @param msg 错误消息
     * @return 自定义状态的降级结果
     */
    public static DeepSeekEvalResult fallback(String status, String msg) {
        return new DeepSeekEvalResult(
            status,
            0,
            Map.of(
                "fluency", 0,
                "completeness", 0,
                "relevance", 0,
                "pronunciation", 0,
                "grammar", 0,
                "vocabulary", 0
            ),
            new Feedback(
                msg,
                List.of(),
                List.of(),
                List.of(),
                ""
            )
        );
    }

    /**
     * 创建无语音输入的结果
     *
     * <p><b>作用：</b>当音频中没有检测到语音内容时，返回特定的结果。</p>
     * <p><b>场景：</b>
     * <ul>
     *   <li>用户上传空音频</li>
     *   <li>麦克风故障</li>
     *   <li>环境噪音过大</li>
     *   <li>用户没有说话</li>
     * </ul>
     * </p>
     * <p><b>处理：</b>
     * <ul>
     *   <li>状态设为 "no_speech"</li>
     *   <li>所有评分为 0</li>
     *   <li>提供明确的错误提示</li>
     *   <li>给出具体的操作建议</li>
     * </ul>
     * </p>
     *
     * @return 无语音输入的评估结果
     */
    public static DeepSeekEvalResult noSpeech() {
        return new DeepSeekEvalResult(
            "no_speech",
            0,
            Map.of(
                "fluency", 0,
                "completeness", 0,
                "relevance", 0,
                "pronunciation", 0,
                "grammar", 0,
                "vocabulary", 0
            ),
            new Feedback(
                "No speech detected in the audio. Please speak clearly and try again.",
                List.of(),
                List.of(),
                List.of("Ensure your microphone is working", "Speak louder and more clearly"),
                ""
            )
        );
    }

    /**
     * 判断评估是否成功
     *
     * <p><b>作用：</b>检查当前评估结果是否成功完成。</p>
     * <p><b>成功条件：</b>状态为 "success" 且综合评分大于 0</p>
     *
     * @return true 如果评估成功，false 否则
     */
    public boolean isSuccessful() {
        return "success".equals(status) && overallScore != null && overallScore > 0;
    }

    /**
     * 获取特定的评分
     *
     * <p><b>作用：</b>从 metrics 中获取指定维度的评分。</p>
     * <p><b>安全处理：</b>如果 metrics 中不存在该维度，返回 0</p>
     *
     * @param metric 评分维度名称
     * @return 对应维度的评分（0-100），如果不存在返回 0
     */
    public int getMetric(String metric) {
        if (metrics != null && metrics.containsKey(metric)) {
            return metrics.get(metric);
        }
        return 0;
    }

    /**
     * 检查是否有问题需要改进
     *
     * <p><b>作用：</b>判断反馈中是否包含需要解决的问题。</p>
     * <p><b>判断逻辑：</b>issues 列表不为空</p>
     *
     * @return true 如果有需要改进的问题，false 否则
     */
    public boolean hasIssues() {
        return feedback != null && feedback.issues != null && !feedback.issues.isEmpty();
    }

    /**
     * 检查是否有改进建议
     *
     * <p><b>作用：</b>判断反馈中是否包含改进建议。</p>
     * <p><b>判断逻辑：</b>suggestions 列表不为空</p>
     *
     * @return true 如果有改进建议，false 否则
     */
    public boolean hasSuggestions() {
        return feedback != null && feedback.suggestions != null && !feedback.suggestions.isEmpty();
    }

    /**
     * 获取问题类型的数量统计
     *
     * <p><b>作用：</b>统计不同类型问题的数量。</p>
     * <p><b>返回：</b>Map&lt;问题类型, 数量&gt;</p>
     *
     * @return 问题类型统计
     */
    public Map<String, Long> getIssueTypeCount() {
        if (feedback == null || feedback.issues == null) {
            return Map.of();
        }

        return feedback.issues.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                issue -> issue.type,
                java.util.stream.Collectors.counting()
            ));
    }

    /**
     * 创建成功的评估结果（示例方法）
     *
     * <p><b>作用：</b>创建一个成功的评估结果（用于测试或示例）。</p>
     * <p><b>注意：</b>这是示例方法，实际使用时应该从 API 响应创建。</p>
     *
     * @return 成功的评估结果
     */
    public static DeepSeekEvalResult successfulExample() {
        Map<String, Integer> metrics = Map.of(
            "fluency", 85,
            "completeness", 92,
            "relevance", 78,
            "pronunciation", 88,
            "grammar", 90,
            "vocabulary", 85
        );

        List<Issue> issues = List.of(
            new Issue("grammar", "时态使用不当", "建议使用现在完成时"),
            new Issue("vocabulary", "用词重复", "尝试使用同义词替换")
        );

        List<String> strengths = List.of(
            "表达流畅自然",
            "逻辑清晰",
            "内容完整"
        );

        List<String> suggestions = List.of(
            "丰富词汇量，使用更多样化的表达",
            "注意语法细节，提高准确性"
        );

        int overall = metrics.values().stream().mapToInt(Integer::intValue).sum() / metrics.size();

        return new DeepSeekEvalResult(
            "success",
            overall,
            metrics,
            new Feedback(
                "总体表现良好，在流利度和完整性方面表现突出。",
                strengths,
                issues,
                suggestions,
                "我的家乡是一个美丽的海滨城市，拥有丰富的历史文化和美丽的自然景观。这里的人们热情好客，生活节奏舒适宜人。"
            )
        );
    }

    /**
     * 获取最需要改进的维度
     *
     * <p><b>作用：</b>找出评分最低的维度。</p>
     * <p><b>返回：</b>最低评分的维度名称和分数</p>
     *
     * @return 最低维度的 Map.Entry，如果没有数据则返回 null
     */
    public java.util.Map.Entry<String, Integer> getLowestMetric() {
        if (metrics == null || metrics.isEmpty()) {
            return null;
        }

        return metrics.entrySet().stream()
            .min(java.util.Map.Entry.comparingByValue())
            .orElse(null);
    }

    /**
     * 获取评分的平均值
     *
     * <p><b>作用：</b>计算所有维度的平均评分。</p>
     * <p><b>计算方式：</b>所有评分总和 / 维度数量</p>
     *
     * @return 平均评分，如果无数据则返回 0
     */
    public double getAverageScore() {
        if (metrics == null || metrics.isEmpty()) {
            return 0;
        }

        return metrics.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0);
    }
}