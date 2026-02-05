package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评估相关的数据传输对象 (DTO) 集合
 *
 * <p>本类包含所有与文本/音频评估相关的数据传输对象，用于处理用户提交的内容评估请求和返回评估结果。
 * 评估系统支持文本和音频两种模式，提供多维度的评分和详细的分析报告。</p>
 *
 * <p><b>核心功能：</b></p>
 * <ul>
 *   <li>文本评估：评估用户文本的流利度、完整性和相关性</li>
 *   <li>音频评估：结合音频内容进行更全面的评估（音频 URL 必填）</li>
 *   <li>详细分析：定位具体问题并提供改进建议</li>
 *   <li>成长统计：通过 deviceId 跟踪用户进步情况</li>
 * </ul>
 *
 * <p><b>评估维度：</b></p>
 * <ul>
 *   <li><b>流利度 (Fluency)</b>：语言表达的流畅程度和自然度</li>
 *   <li><b>完整性 (Completeness)</b>：回答内容的完整性和覆盖度</li>
 *   <li><b>相关性 (Relevance)</b>：回答与问题的相关性和准确性</li>
 * </ul>
 *
 * <p><b>序列化考虑：</b></p>
 * <ul>
 *   <li>使用 @JsonInclude(NON_NULL) 优化响应体积</li>
 *   <li>时间字段使用 ISO-8601 格式字符串</li>
 *   <li>评分字段使用 Double 类型支持小数精度</li>
 *   <li>使用 Lombok 减少样板代码</li>
 * </ul>
 *
 * <p><b>数据契约：</b></p>
 * <ul>
 *   <li>响应字段保持向后兼容性</li>
 *   <li>新增字段使用可选属性避免破坏性变更</li>
 *   <li>错误信息使用标准化的错误响应格式</li>
 * </ul>
 *
 * <p><b>验证规则：</b></p>
 * <ul>
 *   <li>deviceId: 必填、UUID 格式</li>
 *   <li>prompt: 必填、长度 1-1000 字符</li>
 *   <li>userText: 必填、长度 1-5000 字符</li>
 *   <li>audioUrl: 音频模式时必填、有效的 URL</li>
 *   <li>mode: 可选、"TEXT" 或 "AUDIO"</li>
 *   <li>评分范围：0.0 - 100.0</li>
 * </ul>
 *
 * @author system
 * @since 1.0.0
 */
public class EvalDTO {

    /**
     * 文本评估请求对象
     *
     * <p><b>作用：</b>封装用户提交的文本/音频评估请求</p>
     * <p><b>数据契约：</b>前端评估请求的数据传输对象</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>用户完成练习题目后提交评估</li>
     *   <li>支持纯文本和音频+文本两种评估模式</li>
     *   <li>可选的参考答案和关键词预期</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>deviceId</b> (String, 必填)
     *     <ul>
     *       <li>作用：设备唯一标识符</li>
     *       <li>格式：UUID 或设备唯一 ID</li>
 *       <li>约束：非空、长度不超过 255 字符</li>
     *       <li>用途：用户成长统计、设备管理</li>
     *       <li>重要：此字段用于跟踪用户在不同设备上的学习进度</li>
     *     </ul>
     *   </li>
     *   <li><b>prompt</b> (String, 必填)
     *     <ul>
     *       <li>作用：评估题目/问题</li>
     *       <li>格式：自由文本</li>
     *       <li>约束：非空、长度 1-1000 字符</li>
     *       <li>用途：评估上下文、参考依据</li>
     *     </ul>
     *   </li>
     *   <li><b>userText</b> (String, 必填)
     *     <ul>
     *       <li>作用：用户的回答文本</li>
     *       <li>格式：自由文本</li>
     *       <li>约束：非空、长度 1-5000 字符</li>
     *       <li>用途：评估的核心内容</li>
     *       <li>注意：音频模式时可能包含 ASR 识别的文本</li>
     *     </ul>
     *   </li>
     *   <li><b>audioUrl</b> (String, 可选)
     *     <ul>
     *       <li>作用：用户音频文件地址</li>
     *       <li>格式：有效的 HTTPS URL</li>
     *       <li>约束：音频模式时必填</li>
     *       <li>用途：语音流利度评估、语调分析</li>
     *       <li>支持：MP3、WAV、M4A 等常见音频格式</li>
     *     </ul>
     *   </li>
     *   <li><b>mode</b> (String, 可选)
     *     <ul>
     *       <li>作用：评估模式选择</li>
     *       <li>可选值："TEXT"（纯文本）、"AUDIO"（音频+文本）</li>
     *       <li>默认值："TEXT"（不传时默认文本模式）</li>
     *       <li>影响：决定是否使用音频进行评估</li>
     *       <li>注意：指定 AUDIO 时 audioUrl 必须提供</li>
     *     </ul>
     *   </li>
     *   <li><b>expectedKeywords</b> (List&lt;String&gt;, 可选)
     *     <ul>
     *       <li>作用：预期包含的关键词列表</li>
     *       <li>格式：字符串数组</li>
     *       <li>用途：评估内容完整性参考</li>
     *       <li>处理：为 null 时忽略此字段</li>
     *     </ul>
     *   </li>
     *   <li><b>referenceAnswer</b> (String, 可选)
     *     <ul>
     *       <li>作用：参考答案/标准答案</li>
     *       <li>格式：自由文本</li>
     *       <li>用途：评估内容相关性参考</li>
     *       <li>处理：为 null 时仅基于常识和题目评估</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>验证逻辑：</b></p>
     * <ul>
     *   <li>必填字段校验：deviceId、prompt、userText</li>
     *   <li>模式匹配校验：mode 必须为 null、"TEXT" 或 "AUDIO"</li>
     *   <li>依赖关系校验：mode="AUDIO" 时 audioUrl 不能为空</li>
     *   <li>长度校验：各字段长度在允许范围内</li>
     *   <li>格式校验：URL 格式、特殊字符等</li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * {
     *   "deviceId": "550e8400-e29b-41d4-a716-446655440000",
     *   "prompt": "请描述一下你的家乡",
     *   "userText": "我的家乡是一个美丽的海滨城市，有三座跨海大桥...",
     *   "audioUrl": "https://example.com/audio/user_answer_123.mp3",
     *   "mode": "AUDIO",
     *   "expectedKeywords": ["家乡", "城市", "风景"],
     *   "referenceAnswer": "我的家乡位于东部沿海，以美丽的海滩和海鲜闻名..."
     * }
     * </pre>
     */
    @Data
    public static class TextEvalReq {
        private String deviceId;            // 设备ID (用于Growth统计)
        private String prompt;              // 题目
        private String userText;            // 用户回答
        private String audioUrl;            // 音频URL (可选)
        private String mode;                // 评估模式 (可选: "TEXT", "AUDIO")

        // 可选字段，允许为 null
        private List<String> expectedKeywords;
        private String referenceAnswer;
    }

    /**
     * 文本评估响应对象
     *
     * <p><b>作用：</b>封装评估系统返回的详细结果和评分</p>
     * <p><b>数据契约：</b>评估结果的标准响应格式</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>用户提交评估后获取详细评分和分析</li>
     *   <li>前端展示评估结果和改进建议</li>
     *   <li>历史记录保存和查询</li>
     *   <li>用户成长数据分析</li>
     * </ul>
     * </p>
     *
     * <p><b>字段分组：</b></p>
     * <ul>
     *   <li><b>核心评分</b>： fluency, completeness, relevance</li>
     *   <li><b>详细分析</b>： issues, suggestions</li>
     *   <li><b>资源链接</b>： audioUrl</li>
     *   <li><b>补充信息</b>： userText, recordId, createdAt</li>
     * </ul>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>fluency</b> (Double, 必填)
     *     <ul>
     *       <li>作用：语言流利度评分</li>
     *       <li>范围：0.0 - 100.0</li>
     *       <li>精度：小数点后 1-2 位</li>
     *       <li>评估维度：语法正确性、用词准确性、表达流畅度</li>
     *       <li>计算：基于文本结构和语言模型分析</li>
     *     </ul>
     *   </li>
     *   <li><b>completeness</b> (Double, 必填)
     *     <ul>
     *       <li>作用：内容完整性评分</li>
     *       <li>范围：0.0 - 100.0</li>
     *       <li>评估维度：信息覆盖度、细节丰富度、逻辑完整性</li>
     *       <li>计算：与参考答案/预期关键词对比</li>
     *     </ul>
     *   </li>
     *   <li><b>relevance</b> (Double, 必填)
     *     <ul>
     *       <li>作用：相关性评分</li>
     *       <li>范围：0.0 - 100.0</li>
     *       <li>评估维度：回答针对性、内容准确性、与题目关联度</li>
     *       <li>计算：基于语义相似度和主题匹配度</li>
     *     </ul>
     *   </li>
     *   <li><b>issues</b> (List&lt;Issue&gt;, 必填)
     *     <ul>
     *       <li>作用：发现的问题列表</li>
     *       <li>格式：Issue 对象数组</li>
     *       <li>内容：语法错误、逻辑问题、表达不当等</li>
     *       <li>位置：包含 offset 和 length 定位问题位置</li>
     *     </ul>
     *   </li>
     *   <li><b>suggestions</b> (List&lt;String&gt;, 必填)
     *     <ul>
     *       <li>作用：改进建议列表</li>
     *       <li>格式：字符串数组</li>
     *       <li>内容：针对问题的具体改进方法</li>
     *       <li>用途：帮助用户理解如何改进</li>
     *     </ul>
     *   </li>
     *   <li><b>audioUrl</b> (String, 可选)
     *     <ul>
     *       <li>作用：评估使用的音频文件地址</li>
     *       <li>格式：有效的 HTTPS URL</li>
     *       <li>用途：回放查看、音频分析结果关联</li>
     *       <li>条件：音频评估模式时返回</li>
     *     </ul>
     *   </li>
     *   <li><b>userText</b> (String, 可选)
     *     <ul>
     *       <li>作用：ASR 识别或原始用户文本</li>
     *       <li>用途：展示用户实际提交的内容</li>
     *       <li>注意：可能不同于提交的文本（如有 ASR）</li>
     *     </ul>
     *   </li>
     *   <li><b>recordId</b> (Long, 可选)
     *     <ul>
     *       <li>作用：数据库记录 ID</li>
     *       <li>格式：数据库自增 ID</li>
     *       <li>用途：历史记录查询、数据关联</li>
     *     </ul>
     *   </li>
     *   <li><b>createdAt</b> (String, 可选)
     *     <ul>
     *       <li>作用：评估创建时间</li>
     *       <li>格式：ISO-8601 时间字符串</li>
     *       <li>示例："2024-01-01T12:00:00Z"</li>
     *       <li>用途：时间排序、历史分析</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>计算方法：</b></p>
     * <ul>
     *   <li><b>总分计算</b>：(fluency + completeness + relevance) / 3.0</li>
     *   <li>仅在三个评分都不为 null 时计算</li>
     *   <li>返回 Double 类型，可能为 null</li>
     * </ul>
     *
     * <p><b>序列化优化：</b></p>
     * <ul>
     *   <li>使用 @JsonInclude(NON_NULL) 减少传输数据量</li>
     *   <li>可选字段为 null 时不序列化</li>
     *   <li>前端可根据需要获取完整信息</li>
     * </ul>
     *
     * <p><b>错误处理：</b></p>
     * <ul>
     *   <li>评分字段为 null：评估失败或未完成</li>
     *   <li>issues 为空：无明显问题</li>
     *   <li>suggestions 为空：无特定建议</li>
     *   <li>网络错误：通过 HTTP 状态码表示</li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * {
     *   "fluency": 85.5,
     *   "completeness": 92.0,
     *   "relevance": 78.5,
     *   "issues": [
     *     {
     *       "offset": 10,
     *       "length": 3,
     *       "message": "用词不当",
     *       "replacements": ["美丽", "漂亮"]
     *     }
     *   ],
     *   "suggestions": [
     *     "建议使用更准确的形容词",
     *     "可以添加更多细节描述"
     *   ],
     *   "audioUrl": "https://example.com/audio/user_answer_123.mp3",
     *   "userText": "我的家乡是一个美丽的海滨城市...",
     *   "recordId": 12345,
     *   "createdAt": "2024-01-01T12:00:00Z"
     * }
     * </pre>
     *
     * <p><b>最佳实践：</b></p>
     * <ul>
     *   <li>前端优先显示三个核心评分</li>
     *   <li>issues 和 suggestions 用于详细反馈</li>
     *   <li>总分由前端计算显示</li>
     *   <li>audioUrl 用于音频模式的结果回放</li>
     * </ul>
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL) // 仅当字段不为null时才返回，减少传输体积
    public static class TextEvalResp {
        // 1. 核心评分 (必须匹配)
        private Double fluency;
        private Double completeness;
        private Double relevance;

        // 2. 详细分析 (必须匹配)
        private List<Issue> issues;
        private List<String> suggestions;

        // 3. 资源链接 (必须匹配)
        private String audioUrl;

        // 4. 补充字段 (后端建议加上，虽然最小契约没写，但前端HistoryDetailView可能需要)
        private String userText; // ASR识别出的文本
        private Long recordId;   // 数据库ID
        private String createdAt; // 创建时间

        // 计算总分的方法
        public Double getOverallScore() {
            if (fluency != null && completeness != null && relevance != null) {
                return (fluency + completeness + relevance) / 3.0;
            }
            return null;
        }

        public Double getFluency() {
            return fluency;
        }

        public void setFluency(Double fluency) {
            this.fluency = fluency;
        }

        public Double getCompleteness() {
            return completeness;
        }

        public void setCompleteness(Double completeness) {
            this.completeness = completeness;
        }

        public Double getRelevance() {
            return relevance;
        }

        public void setRelevance(Double relevance) {
            this.relevance = relevance;
        }

        public List<Issue> getIssues() {
            return issues;
        }

        public void setIssues(List<Issue> issues) {
            this.issues = issues;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }

        public String getUserText() {
            return userText;
        }

        public void setUserText(String userText) {
            this.userText = userText;
        }
    }

    /**
     * 问题详情对象
     *
     * <p><b>作用：</b>封装评估发现的具体问题和改进建议</p>
     * <p><b>数据契约：</b>TextEvalResp 中 issues 字段的详细结构</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>定位文本中的具体问题位置</li>
     *   <li>提供问题类型和描述</li>
     *   <li>提供改进建议和替代方案</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>offset</b> (Integer, 必填)
     *     <ul>
     *       <li>作用：问题在文本中的起始位置（字符偏移量）</li>
     *       <li>格式：非负整数</li>
     *       <li>单位：字符数</li>
     *       <li>用途：定位问题位置、高亮显示</li>
     *     </ul>
     *   </li>
     *   <li><b>length</b> (Integer, 必填)
     *     <ul>
     *       <li>作用：问题的字符长度</li>
     *       <li>格式：正整数</li>
     *       <li>用途：确定问题范围、高亮区域</li>
     *     </ul>
     *   </li>
     *   <li><b>message</b> (String, 必填)
     *     <ul>
     *       <li>作用：问题描述</li>
     *       <li>格式：自由文本</li>
     *       <li>内容：问题类型和具体说明</li>
     *       <li>示例："用词不当"、"语法错误"、"表达模糊"</li>
     *     </ul>
     *   </li>
     *   <li><b>replacements</b> (List&lt;String&gt;, 可选)
     *     <ul>
     *       <li>作用：推荐的替代方案</li>
     *       <li>格式：字符串数组</li>
     *       <li>用途：提供具体的改进建议</li>
     *       <li>处理：为空时无具体建议</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>使用示例：</b></p>
     * <pre>
     * // 原始文本："我的家乡是一个很美丽的地方"
     * // 问题：很美丽 → 用词不当
     *
     * {
     *   "offset": 7,
     *   "length": 3,
     *   "message": "用词不当",
     *   "replacements": ["非常美丽", "特别美丽", "十分美丽"]
     * }
     * </pre>
     *
     * <p><b>前端处理：</b></p>
     * <ul>
     *   <li>根据 offset 和 length 高亮问题区域</li>
     *   <li>显示问题描述 message</li>
     *   <li>如果 replacements 不为空，显示建议选项</li>
     *   <li>支持点击查看详细解释</li>
     * </ul>
     */
    @Data
    public static class Issue {
        private Integer offset;
        private Integer length;
        private String message;
        private List<String> replacements;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 错误响应对象
     *
     * <p><b>作用：</b>封装评估过程中的错误信息</p>
     * <p><b>数据契约：</b>标准化的错误响应格式</p>
     * <p><b>使用场景：</b>
     * <ul>
     *   <li>请求参数验证失败</li>
     *   <li>评估服务异常</li>
     *   <li>网络或系统错误</li>
     *   <li>权限或资源限制</li>
     * </ul>
     * </p>
     *
     * <p><b>字段说明：</b></p>
     * <ul>
     *   <li><b>code</b> (String, 必填)
     *     <ul>
     *       <li>作用：错误代码</li>
     *       <li>格式：字符串</li>
     *       <li>规范：使用标准 HTTP 状态码或自定义错误码</li>
     *       <li>示例："400"、"INVALID_REQUEST"、"EVAL_FAILED"</li>
     *       <li>用途：错误分类和处理</li>
     *     </ul>
     *   </li>
     *   <li><b>message</b> (String, 必填)
     *     <ul>
     *       <li>作用：错误描述信息</li>
     *       <li>格式：自由文本</li>
     *       <li>内容：详细的错误说明</li>
     *       <li>示例："参数验证失败"、"评估服务暂时不可用"</li>
     *       <li>用途：用户提示和调试</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>错误码规范：</b></p>
     * <ul>
     *   <li><b>4xx - 客户端错误</b>
     *     <ul>
     *       <li>400 - 请求参数错误</li>
     *       <li>401 - 未认证</li>
     *       <li>403 - 权限不足</li>
     *       <li>404 - 资源不存在</li>
     *     </ul>
     *   </li>
     *   <li><b>5xx - 服务端错误</b>
     *     <ul>
     *       <li>500 - 内部服务器错误</li>
     *       <li>503 - 服务不可用</li>
     *       <li>504 - 网关超时</li>
     *     </ul>
     *   </li>
     *   <li><b>自定义错误码</b>
     *     <ul>
     *       <li>EVAL_FAILED - 评估失败</li>
     *       <li>ASR_ERROR - 语音识别错误</li>
     *       <li>TIMEOUT - 评估超时</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <p><b>示例：</b></p>
     * <pre>
     * // 参数验证错误
     * {
     *   "code": "400",
     *   "message": "userText 字段不能为空"
     * }
     *
     * // 评估失败
     * {
     *   "code": "EVAL_FAILED",
     *   "message": "评估服务暂时不可用，请稍后重试"
     * }
     *
     * // 网络超时
     * {
     *   "code": "TIMEOUT",
     *   "message": "评估请求超时，请检查网络连接"
     * }
     * </pre>
     *
     * <p><b>前端处理：</b></p>
     * <ul>
     *   <li>根据 code 显示不同的错误图标和样式</li>
     *   <li>显示 message 作为错误详情</li>
     *   <li>根据错误类型提供重试或返回建议</li>
     *   <li>记录错误日志用于调试</li>
     * </ul>
     */
    @Data
    public static class ErrorResp {
        private String code;
        private String message;

        public ErrorResp(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
