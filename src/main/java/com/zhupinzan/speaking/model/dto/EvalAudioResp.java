package com.zhupinzan.speaking.model.dto;

import java.util.List;

/**
 * 评估音频响应对象
 *
 * <p><b>作用：</b>封装音频评估的完整结果，包含语音识别文本、详细评分和改进建议。
 * 专门用于音频模式的评估响应，提供更丰富的语音相关分析。</p>
 *
 * <p><b>设计特点：</b></p>
 * <ul>
 *   <li>使用 Java Record 确保数据不可变性</li>
 *   <li>包含音频识别的原始文本</li>
 *   <li>提供语音相关的专项评分</li>
 *   <li>支持语音特化的改进建议</li>
 *   <li>包含音频时长信息</li>
 * </ul>
 *
 * <p><b>与 TextEvalResp 的区别：</b></p>
 * <ul>
 *   <li>音频评估包含语音识别结果</li>
 *   <li>评分范围是整数（0-100）</li>
 *   <li>添加了音频时长字段</li>
 *   <li>反馈和建议更加语音化</li>
 *   <li>没有 issues 数组（音频问题已包含在 feedback 中）</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>所有字段序列化为 JSON 对象</li>
 *   <li>时间单位：毫秒（durationMs）</li>
 *   <li>评分字段使用整数格式</li>
 *   <li>suggestions 数组支持 JSON 数组格式</li>
 * </ul>
 *
 * <p><b>验证规则：</b></p>
 * <ul>
 *   <li>recordId：正整数，非空</li>
 *   <li>audioUrl：有效的 HTTPS URL</li>
 *   <li>durationMs：正整数，单位毫秒</li>
 *   <li>transcript：非空字符串，语音识别文本</li>
 *   <li>评分字段：0-100 范围内的整数</li>
 *   <li>feedback：非空字符串，详细反馈</li>
 *   <li>suggestions：字符串数组，可为空</li>
 * </ul>
 *
 * <p><b>数据流：</b></p>
 * <ul>
 *   <li>1. 音频上传 → 语音识别 → 内容评估</li>
 *   <li>2. 语音分析 → 流畅度评分</li>
 *   <li>3. 文本评估 → 内容评分</li>
 *   <li>4. 生成反馈和建议 → 组装响应</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public record EvalAudioResp(
        Long recordId,
        String audioUrl,
        long durationMs,
        String transcript,
        int overallScore,
        int fluency,
        int completeness,
        int relevance,
        String feedback,
        List<String> suggestions) {

    /**
     * 评估记录 ID
     *
     * <p><b>作用：</b>唯一标识一次评估记录</p>
     * <p><b>数据类型：</b>Long (数据库主键)</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>正整数 (> 0)</li>
     *   <li>数据库自增 ID</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>数据库表主键</li>
     *   <li>历史记录查询标识</li>
     *   <li>与其他数据表关联</li>
     *   <li>前端显示记录编号</li>
     * </ul>
     * </p>
     */
    public Long recordId() {
        return recordId;
    }

    /**
     * 音频文件 URL
     *
     * <p><b>作用：</b>指向用户上传的音频文件</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>格式：</b>有效的 HTTPS URL</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度不超过 2048 字符</li>
     *   <li>必须使用 HTTPS 协议</li>
     *   <li>支持常见音频格式：MP3、WAV、M4A、OGG</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>https://cdn.example.com/audios/user_123/answer_456.mp3</p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>音频回放功能</li>
     *   <li>结果页面音频展示</li>
     *   <li>下载原始音频</li>
     *   <li>问题复现和审核</li>
     * </ul>
     * </p>
     */
    public String audioUrl() {
        return audioUrl;
    }

    /**
     * 音频时长（毫秒）
     *
     * <p><b>作用：</b>记录音频文件的播放时长</p>
     * <p><b>数据类型：</b>long</p>
     * <p><b>单位：</b>毫秒</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非负整数 (>= 0)</li>
     *   <li>典型范围：1,000 - 300,000（1秒 - 5分钟）</li>
     *   <li>最大限制：3,600,000（1小时）</li>
     * </ul>
     * </p>
     * <p><b>计算方式：</b>
     * <ul>
     *   <li>从音频文件元数据获取</li>
     *   <li>服务器端精确计算</li>
     *   <li>前端显示时转换为可读格式</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>评估时长合理性</li>
     *   <li>计算语速（words per minute）</li>
     *   <li>用户时长统计</li>
     *   <li>界面显示时间格式化</li>
     * </ul>
     * </p>
     */
    public long durationMs() {
        return durationMs;
    }

    /**
     * 语音识别文本
     *
     * <p><b>作用：</b>从音频中识别出的原始文本内容</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>生成方式：</b>ASR（自动语音识别）转换</p>
     * <p><b>约束条件：</b>
     * <ul>
     *   <li>非空（必填）</li>
     *   <li>长度不超过 10000 字符</li>
     *   <li>包含可能识别错误的词汇</li>
     *   <li>保持语音的原始语序和停顿</li>
     * </ul>
     * </p>
     * <p><b>准确度：</b>
     * <ul>
     *   <li>受音频质量影响</li>
     *   <li>受口音和语速影响</li>
     *   <li>专业领域词汇识别率较低</li>
     *   <li>可能需要后校准</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>文本内容评估的基础</li>
     *   <li>与用户提交文本对比</li>
     *   <li>展示识别结果</li>
     *   <li>改进识别算法的训练数据</li>
     * </ul>
     * </p>
     */
    public String transcript() {
        return transcript;
    }

    /**
     * 综合评分
     *
     * <p><b>作用：</b>音频评估的总体表现得分</p>
     * <p><b>数据类型：</b>int</p>
     * <p><b>评分范围：</b>0 - 100</p>
     * <p><b>计算方式：</b>(fluency + completeness + relevance) / 3</p>
     * <p><b>四舍五入：</b>取整数</p>
     * <p><b>评分标准：</b>
     * <ul>
     *   <li>90-100：优秀</li>
     *   <li>80-89：良好</li>
     *   <li>70-79：中等</li>
     *   <li>60-69：及格</li>
     *   <li>0-59：不及格</li>
     * </ul>
     * </p>
     * <p><b>用途：</b>
     * <ul>
     *   <li>快速了解整体表现</li>
     *   <li>排行榜和成就系统</li>
     *   <li>学习进度跟踪</li>
     *   <li>与其他用户对比</li>
     * </ul>
     * </p>
     */
    public int overallScore() {
        return overallScore;
    }

    /**
     * 流利度评分
     *
     * <p><b>作用：</b>评估用户语音表达的流畅程度</p>
     * <p><b>数据类型：</b>int</p>
     * <p><b>评分范围：</b>0 - 100</p>
     * <p><b>评估维度：</b>
     * <ul>
     *   <li><b>发音清晰度</b>：发音是否清晰准确</li>
     *   <li><b>语速适中性</b>：语速是否自然流畅</li>
     *   <li><b>停顿合理性</b>：停顿位置和时长是否恰当</li>
     *   <li><b>语调变化</b>：是否有自然的语调起伏</li>
     *   <li><b>流畅连贯性</b>：表达是否流畅无卡顿</li>
     * </ul>
     * </p>
     * <p><b>技术实现：</b>
     * <ul>
     *   <li>音频质量分析</li>
     *   <li>语速计算（words per minute）</li>
     *   <li>停顿检测和频率分析</li>
     *   <li>声学特征提取</li>
     *   <li>AI 模型评分</li>
     * </ul>
     * </p>
     * <p><b>常见问题：</b>
     * <ul>
     *   <li>语速过快/过慢</li>
     *   <li>过多不必要的停顿</li>
     *   <li>发音不清或错误</li>
     *   <li>语调单调</li>
     *   <li>重复和修正过多</li>
     * </ul>
     * </p>
     */
    public int fluency() {
        return fluency;
    }

    /**
     * 完整性评分
     *
     * <p><b>作用：</b>评估用户回答内容的完整程度</p>
     * <p><b>数据类型：</b>int</p>
     * <p><b>评分范围：</b>0 - 100</p>
     * <p><b>评估维度：</b>
     * <ul>
     *   <li><b>信息覆盖度</b>：是否覆盖了问题的所有要点</li>
     *   <li><b>细节丰富度</b>：是否有足够的细节支持</li>
     *   <li><b>逻辑完整性</b>：逻辑链条是否完整</li>
     *   <li><b>回答长度</b>：回答是否充分展开</li>
     * </ul>
     * </p>
     * <p><b>评估标准：</b>
     * <ul>
     *   <li><b>100分</b>：内容详实，覆盖所有要点，细节丰富</li>
     *   <li><b>80-89分</b>：内容完整，基本覆盖要点，细节足够</li>
     *   <li><b>60-79分</b>：主要内容覆盖，部分细节缺失</li>
     *   <li><b>40-59分</b>：部分要点覆盖，内容不够完整</li>
     *   <li><b>0-39分</b>：要点遗漏严重，内容过于简略</li>
     * </ul>
     * </p>
     * <p><b>影响因素：</b>
     * <ul>
     *   <li>与参考答案的对比分析</li>
     *   <li>关键词检测和匹配度</li>
     *   <li>内容长度分析</li>
     *   <li>语义完整性评估</li>
     * </ul>
     * </p>
     */
    public int completeness() {
        return completeness;
    }

    /**
     * 相关性评分
     *
     * <p><b>作用：</b>评估用户回答与题目的相关程度</p>
     * <p><b>数据类型：</b>int</p>
     * <p><b>评分范围：</b>0 - 100</p>
     * <p><b>评估维度：</b>
     * <ul>
     *   <li><b>主题相关性</b>：回答是否紧扣主题</li>
     *   <li><b>准确性</b>：内容是否准确无误</li>
     *   <li><b>针对性</b>：是否直接回答了问题</li>
     *   <li><b>逻辑性</b>：论证是否合理</li>
     * </ul>
     * </p>
     * <p><b>评估方法：</b>
     * <ul>
     *   <li>语义相似度计算</li>
     *   <li>关键词匹配分析</li>
     *   <li>主题模型分析</li>
     *   <li>人工评估校准</li>
     * </ul>
     * </p>
     * <p><b>常见问题：</b>
     * <ul>
     *   <li>答非所问</li>
     *   <li>偏离主题</li>
     *   <li>信息不准确</li>
     *   <li>缺乏针对性</li>
     * </ul>
     * </p>
     */
    public int relevance() {
        return relevance;
    }

    /**
     * 详细反馈
     *
     * <p><b>作用：</b>针对音频表现的详细评价和指导</p>
     * <p><b>数据类型：</b>String</p>
     * <p><b>内容特点：</b>
     * <ul>
     *   <li>语音相关的具体评价</li>
     *   <li>指出优点和需要改进的地方</li>
     *   <li>提供具体的技术指导</li>
     *   <li>鼓励性的语言</li>
     * </ul>
     * </p>
     * <p><b>反馈结构：</b>
     * <ul>
     *   <li><b>开头</b>：总体评价</li>
     *   <li><b>中间</b>：具体优缺点分析</li>
     *   <li><b>结尾</b>：改进建议和鼓励</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>"您的发音很清晰，语速适中。建议在回答问题时增加一些停顿，这样可以让表达更有层次感。整体表现不错，继续保持！"</p>
     */
    public String feedback() {
        return feedback;
    }

    /**
     * 改进建议列表
     *
     * <p><b>作用：</b>提供具体可操作的改进建议</p>
     * <p><b>数据类型：</b>List&lt;String&gt;</p>
     * <p><b>内容特点：</b>
     * <ul>
     *   <li>针对性强，针对具体问题</li>
     *   <li>可操作，有明确的改进方法</li>
     *   <li>分点列出，易于理解和执行</li>
     *   <li>循序渐进，由易到难</li>
     * </ul>
     * </p>
     * <p><b>建议类型：</b>
     * <ul>
     *   <li><b>发音建议</b>：特定音节的发音技巧</li>
     *   <li><b>语速建议</b>：如何调整语速</li>
     *   <li><b>停顿建议</b>：在哪里停顿以及如何停顿</li>
     *   <li><b>内容建议</b>：如何完善回答内容</li>
     *   <li><b>表达建议</b>：如何让表达更生动</li>
     * </ul>
     * </p>
     * <p><b>示例：</b>
     * <pre>
     * [
     *   "练习'zh'、'ch'、'sh'的发音，可以用舌头抵住上齿龈",
     *   "适当放慢语速，每分钟 120-150 字为宜",
     *   "在重要观点前加入适当停顿",
     *   "回答时尝试使用'首先'、'其次'等过渡词"
     * ]
     * </pre>
     */
    public List<String> suggestions() {
        return suggestions;
    }

    // 以下是常用的工具方法和格式化输出

    /**
     * 获取音频时长格式化字符串
     *
     * <p><b>作用：</b>将毫秒转换为易读的时间格式</p>
     * <p><b>返回格式：</b>mm:ss 或 hh:mm:ss</p>
     * <p><b>示例：</b>01:23 或 10:25:30</p>
     *
     * @return 格式化的时间字符串
     */
    public String getFormattedDuration() {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes >= 60) {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
        } else {
            return String.format("%02d:%02d", minutes, remainingSeconds);
        }
    }

    /**
     * 获取评分等级描述
     *
     * <p><b>作用：</b>根据综合评分返回等级描述</p>
     *
     * @return 评分等级（优秀、良好、中等、及格、不及格）
     */
    public String getScoreLevel() {
        if (overallScore >= 90) return "优秀";
        if (overallScore >= 80) return "良好";
        if (overallScore >= 70) return "中等";
        if (overallScore >= 60) return "及格";
        return "不及格";
    }

    /**
     * 检查是否通过评估
     *
     * <p><b>作用：</b>判断用户是否达到及格线</p>
     * <p><b>及格线：</b>60分</p>
     *
     * @return true 如果及格，false 如果不及格
     */
    public boolean isPass() {
        return overallScore >= 60;
    }

    /**
     * 计算语速（字/分钟）
     *
     * <p><b>作用：</b>根据音频时长和识别文本计算语速</p>
     * <p><b>公式：</b>语速 = (transcript.length() / durationMs) * 60000</p>
     *
     * @return 语速（字/分钟）
     */
    public double getWordsPerMinute() {
        if (durationMs == 0) return 0;
        return (transcript.length() * 60000.0) / durationMs;
    }

    /**
     * 获取语速评价
     *
     * <p><b>作用：</b>根据语速给出评价</p>
     * <p><b>标准：</b>
     * <ul>
     *   <li>&lt; 150：过慢</li>
     *   <li>150-250：适中</li>
     *   <li>250-350：稍快</li>
     *   &gt; 350：过快
     * </ul>
     *
     * @return 语速评价字符串
     */
    public String getSpeedEvaluation() {
        double wpm = getWordsPerMinute();
        if (wpm < 150) return "语速偏慢，可以适当加快";
        if (wpm <= 250) return "语速适中，很好";
        if (wpm <= 350) return "语速稍快，可以适当放慢";
        return "语速偏快，建议放慢语速";
    }
}