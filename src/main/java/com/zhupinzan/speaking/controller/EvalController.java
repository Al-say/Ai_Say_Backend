package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalAudioResp;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.service.AuthUserService;
import com.zhupinzan.speaking.service.DeepSeekEvalService;
import com.zhupinzan.speaking.service.EvalOrchestratorService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 口语能力评估REST控制器
 * <p>
 * 该控制器是口语评估系统的核心API入口，负责处理所有与用户口语能力评估相关的请求。
 * 系统支持两种主要的评估方式：文本评估和音频评估，分别针对不同的用户场景需求。
 *
 * <h3>核心功能：</h3>
 * <ul>
 *     <li>文本评估：基于用户输入的文字内容进行实时评分和分析</li>
 *     <li>音频评估：处理用户上传的语音文件，提供全面的口语能力评估</li>
 *     <li>智能反馈：提供详细的改进建议和问题分析</li>
 * </ul>
 *
 * <h3>业务流程：</h3>
 * <ol>
 *     <li>接收客户端请求并进行参数校验</li>
 *     <li>根据请求类型调用相应的服务处理</li>
 *     <li>处理服务层返回的评估结果</li>
 *     <li>构建标准化的响应返回给客户端</li>
 *     <li>处理各种异常情况并返回适当的错误信息</li>
 * </ol>
 *
 * <h3>API端点：</h3>
 * <ul>
 *     <li>POST /api/eval/text - 文本评估接口</li>
 *     <li>POST /api/eval/audio - 简化的音频评估接口（待废弃）</li>
 *     <li>POST /api/eval/audio/full - 完整音频评估接口</li>
 * </ul>
 *
 * <h3>错误处理：</h3>
 * <ul>
 *     <li>400 Bad Request - 请求参数无效或缺失</li>
 *     <li>500 Internal Server Error - 评估处理失败</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/eval")
@RequiredArgsConstructor
public class EvalController {

    /**
     * 评估编排服务
     * <p>
     * 负责协调完整的音频评估流程，包括：
     * - 音频文件处理
     * - 语音识别 (ASR)
     * - AI智能评估
     * - 结果持久化
     * - 用户进度更新
     */
    private final EvalOrchestratorService evalOrchestratorService;

    /**
     * DeepSeek评估服务
     * <p>
     * 专门用于处理文本评估，调用DeepSeek AI接口进行：
     * - 文本质量分析
     * - 相关性评分
     * - 完整性评估
     * - 生成改进建议
     */
    private final DeepSeekEvalService deepSeekEvalService;

    /**
     * 认证用户服务
     * <p>
     * 负责处理用户认证相关的操作：
     * - 设备ID验证和绑定
     * - 用户身份校验
     * - 权限控制
     */
    private final AuthUserService authUserService;

    /**
     * 文本评估API端点
     * <p>
     * 该接口用于评估用户输入的文字内容质量，适用于以下场景：
     * - 文字练习后的即时反馈
     * - 文章写作能力评估
     * - 口语表达的文字模拟
     *
     * <h3>请求结构：</h3>
     * <pre>
     * POST /api/eval/text
     * Content-Type: application/json
     *
     * {
     *     "prompt": "描述你对未来的规划",
     *     "userText": "我希望成为一名工程师，为社会做出贡献",
     *     "expectedKeywords": ["工程师", "贡献"],
     *     "referenceAnswer": "未来规划应包括职业目标和个人发展"
     * }
     * </pre>
     *
     * <h3>响应结构：</h3>
     * <pre>
     * {
     *     "fluency": 85.0,        // 流畅度评分 (0-100)
     *     "completeness": 75.0,   // 完整性评分 (0-100)
     *     "relevance": 90.0,       // 相关性评分 (0-100)
     *     "issues": [
     *         {
     *             "offset": 10,
     *             "length": 4,
     *             "message": "可以更具体地描述",
     *             "replacements": ["详细规划"]
     *         }
     *     ],
     *     "suggestions": ["加入更多细节", "使用更专业的词汇"],
     *     "userText": "我希望成为一名工程师，为社会做出贡献",
     *     "recordId": 123,
     *     "createdAt": "2024-01-01T10:00:00"
     * }
     * </pre>
     *
     * <h3>业务逻辑：</h3>
     * <ol>
     *     <li><b>参数校验</b>：检查prompt和userText是否为空</li>
     *     <li><b>设备验证</b>：确保用户已绑定设备ID</li>
     *     <li><b>AI评估</b>：调用DeepSeek服务进行文本分析</li>
     *     <li><b>结果映射</b>：将AI返回的结果转换为标准DTO</li>
     *     <li><b>返回响应</b>：返回评估结果给客户端</li>
     * </ol>
     *
     * <h3>错误码：</h3>
     * <ul>
     *     <li>BAD_REQUEST - 请求参数无效或缺失</li>
     *     <li>EVAL_FAILED - AI评估过程失败</li>
     * </ul>
     *
     * @param req     请求体，包含提示词和用户输入的文本。详细字段说明：
     *               - prompt: 评估的题目或要求
     *               - userText: 用户提交的回答内容
     *               - expectedKeywords: 预期的关键词列表（可选）
     *               - referenceAnswer: 参考答案（可选）
     * @param user    当前登录用户信息（通过@CurrentUser注解注入）
     * @param persona 用户画像，决定了AI评估的标准和风格，默认为EXAM_PREP（备考党）
     * @return ResponseEntity 包含评估结果或错误信息
     *         - 成功：200 OK，返回TextEvalResp对象
     *         - 参数错误：400 Bad Request，返回ErrorResp对象
     *         - 评估失败：500 Internal Server Error，返回ErrorResp对象
     */
    @PostMapping("/text")
    public ResponseEntity<?> evalText(@RequestBody EvalDTO.TextEvalReq req,
            @CurrentUser CurrentUserInfo user,
            @RequestParam(value = "persona", defaultValue = "EXAM_PREP") UserPersona persona) {
        // 步骤 1: 基本的参数校验 - 确保核心评估参数不为空
        if (req.getPrompt() == null || req.getPrompt().trim().isEmpty() ||
                req.getUserText() == null || req.getUserText().trim().isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EvalDTO.ErrorResp("BAD_REQUEST", "prompt 或 userText 不能为空"));
        }

        try {
            // 步骤 2: 验证用户设备绑定 - 确保用户身份可追踪
            authUserService.requireDeviceId(user);

            // 步骤 3: 调用DeepSeek服务进行评估，并映射为响应DTO
            var evalResult = deepSeekEvalService.evaluate(persona, req.getPrompt(), req.getUserText());
            EvalDTO.TextEvalResp resp = deepSeekEvalService.mapToTextEvalResp(evalResult, req.getUserText());

            // 步骤 4: 返回评估结果
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            // 步骤 5: 统一的异常处理 - 捕获并格式化所有异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("EVAL_FAILED", "评估失败: " + e.getMessage()));
        }
    }

    /**
     * 简化音频评估端点 (已标记为待废弃)
     * <p>
     * 这是一个临时的音频评估接口，仅用于快速验证和测试目的。
     * 该接口返回模拟的评估数据，不进行实际的音频处理和分析。
     *
     * <h3>警告：</h3>
     * <ul>
     *     <li>该接口仅返回固定数值，不提供真实的评估结果</li>
     *     <li>不会处理音频文件内容或进行语音识别</li>
     *     <li>建议尽快迁移到 {@code /api/eval/audio/full} 接口</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *     <li>前端原型开发和演示</li>
     *     <li>快速功能验证</li>
     *     <li>临时测试环境</li>
     * </ul>
     *
     * <h3>请求参数：</h3>
     * <ul>
     *     <li>file: 音频文件 (MultipartFile)</li>
     *     <li>prompt: 可选的评估提示</li>
     *     <li>persona: 用户画像，默认为EXAM_PREP</li>
     * </ul>
     *
     * <h3>模拟响应数据：</h3>
     * <pre>
     * {
     *     "fluency": 80.0,
     *     "completeness": 75.0,
     *     "relevance": 85.0,
     *     "suggestions": ["发音清晰", "注意语调"]
     * }
     * </pre>
     *
     * @deprecated 请使用 {@code POST /api/eval/audio/full} 获得完整的音频评估功能。
     *             该接口将在未来版本中移除。
     *
     * @param file    用户上传的音频文件
     * @param prompt  可选的评估提示词
     * @param persona 用户画像，影响评估标准
     * @ ResponseEntity 包含模拟评估结果或错误信息
     */
    @PostMapping("/audio")
    public ResponseEntity<?> evalAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "persona", defaultValue = "EXAM_PREP") UserPersona persona) {

        // 参数校验：确保音频文件存在
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new EvalDTO.ErrorResp("BAD_REQUEST", "音频文件不能为空"));
        }

        try {
            // 返回模拟的评估结果（仅用于演示）
            EvalDTO.TextEvalResp resp = new EvalDTO.TextEvalResp();
            resp.setFluency(80.0);    // 固定流畅度分数
            resp.setCompleteness(75.0); // 固定完整度分数
            resp.setRelevance(85.0);   // 固定相关度分数
            resp.setSuggestions(java.util.List.of("发音清晰", "注意语调")); // 固定建议

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("AUDIO_PROCESS_FAILED", "处理失败: " + e.getMessage()));
        }
    }

    /**
     * 完整音频评估API端点
     * <p>
     * 该接口是系统的核心音频评估功能，提供完整的口语能力分析流程。
     * 它不仅评估用户的发音和表达，还提供详细的AI反馈和改进建议。
     *
     * <h3>完整评估流程：</h3>
     * <ol>
     *     <li><b>音频处理</b>：接收并验证音频文件</li>
     *     <li><b>语音识别</b>：使用ASR技术将语音转换为文字</li>
     *     <li><b>能力评估</b>：AI分析口语表现的多个维度</li>
     *     <li><b>反馈生成</b>：提供针对性的改进建议</li>
     *     <li><b>数据存储</li>：将评估结果保存到数据库</li>
     *     <li><b>进度更新</li>：更新用户的练习统计数据</li>
     * </ol>
     *
     * <h3>请求结构：</h3>
     * <pre>
     * POST /api/eval/audio/full
     * Content-Type: multipart/form-data
     *
     * --boundary
     * Content-Disposition: form-data; name="persona"
     * EXAM_PREP
     *
     * --boundary
     * Content-Disposition: form-data; name="scene"
     * 面试准备
     *
     * --boundary
     * Content-Disposition: form-data; name="audio"; filename="recording.mp3"
     * [音频文件内容]
     * --boundary--
     * </pre>
     *
     * <h3>响应结构：</h3>
     * <pre>
     * {
     *     "recordId": 12345,              // 评估记录ID
     *     "audioUrl": "/api/audio/12345", // 音频访问URL
     *     "durationMs": 45000,            // 音频时长（毫秒）
     *     "transcript": "你好，我叫张三...", // 语音识别结果
     *     "overallScore": 82,             // 总体评分（0-100）
     *     "fluency": 80,                 // 流畅度评分
     *     "completeness": 75,            // 完整性评分
     *     "relevance": 90,                // 相关性评分
     *     "feedback": "你的表达很清晰...", // AI详细反馈
     *     "suggestions": [
     *         "注意语速变化",
     *         "增加一些例子",
     *         "使用更专业的词汇"
     *     ]
     * }
     * </pre>
     *
     * <h3>评估维度说明：</h3>
     * <ul>
     *     <li><b>流畅度 (Fluency)</b>：评估语速、停顿、自然程度</li>
     *     <li><b>完整性 (Completeness)</b>：评估内容覆盖度和细节丰富度</li>
     *     <li><b>相关性 (Relevance)</b>：评估回答与主题的契合度</li>
     *     <li><b>总体评分 (Overall Score)</b>：三个维度的综合得分</li>
     * </ul>
     *
     * <h3>支持的音频格式：</h3>
     * <ul>
     *     <li>MP3（推荐）</li>
     *     <li>WAV</li>
     *     <li>M4A</li>
     *     <li>最大文件大小：10MB</li>
     *     <li>最大时长：5分钟</li>
     * </ul>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *     <li>BAD_REQUEST - 音频文件无效或参数缺失</li>
     *     <li>EVAL_FAILED - 评估过程中发生错误</li>
     *     <li>FILE_TOO_LARGE - 音频文件超过大小限制</li>
     *     <li>UNSUPPORTED_AUDIO_FORMAT - 不支持的音频格式</li>
     * </ul>
     *
     * @param user       当前登录用户信息（通过@CurrentUser注解注入）
     * @param persona    用户画像，决定了AI评估的标准和侧重点
     *                   - EXAM_PREP: 备考党，注重考试相关能力
     *                   - BUSINESS: 商务人士，注重职场沟通
     *                   - ACADEMIC: 学术场景，注重学术表达
     * @param scene      评估的具体场景，如"面试准备"、"日常对话"等
     * @param audioFile  用户上传的音频文件（MultipartFile格式）
     *                   - 支持格式：mp3, wav, m4a
     *                   - 最大大小：10MB
     *                   - 最大时长：5分钟
     * @return ResponseEntity 包含详细评估结果
     *         - 成功：200 OK，返回EvalAudioResp对象
     *         - 参数错误：400 Bad Request，返回ErrorResp对象
     *         - 评估失败：500 Internal Server Error，返回ErrorResp对象
     */
    @PostMapping("/audio/full")
    public ResponseEntity<?> evaluateAudio(
            @CurrentUser CurrentUserInfo user,
            @RequestParam("persona") UserPersona persona,
            @RequestParam("scene") String scene,
            @RequestPart("audio") MultipartFile audioFile) {

        // 参数校验：确保音频文件存在且有效
        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new EvalDTO.ErrorResp("BAD_REQUEST", "音频文件不能为空"));
        }

        try {
            // 步骤1：验证用户设备绑定
            var deviceId = authUserService.requireDeviceId(user);

            // 步骤2：委托给评估编排服务处理完整的评估流程
            // 该服务将协调音频处理、语音识别、AI评估等多个步骤
            EvalAudioResp result = evalOrchestratorService.evaluateAudio(deviceId, persona, scene, audioFile);

            // 步骤3：返回评估结果
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // 统一异常处理：捕获并格式化所有评估过程中可能发生的异常
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("EVAL_FAILED", "评估失败: " + e.getMessage()));
        }
    }
}
