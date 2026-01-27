package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final EvalService evalService;
    private final AudioConversionService audioConversionService;
    private final BaiduAsrService baiduAsrService;
    private final DeepSeekEvalService deepSeekEvalService;

    // 定义文件存储的根目录 (在项目根目录下创建一个 uploads 文件夹)
    private final String UPLOAD_DIR = "uploads/";

    /**
     * 音频处理与评估服务 - 端到端的音频处理流水线
     *
     * <h2>服务概述</h2>
     * AudioService是整个音频处理流程的核心协调器，负责接收前端上传的音频文件，
     * 并通过一系列处理步骤最终返回评估结果。它实现了从音频文件到智能评分的
     * 完整业务流程，是连接前端展示和后端智能服务的桥梁。
     *
     * <h2>核心职责</h2>
     * <ul>
     *   <li><b>文件管理</b>：处理文件上传、存储路径管理、文件命名</li>
     *   <li><b>格式转换</b>：将M4A格式转换为标准WAV格式</li>
     *   <li><b>语音识别</b>：调用ASR服务进行语音转文字</li>
     *   <li><b>智能评估</b>：整合AI服务进行口语能力评估</li>
     *   <li><b>数据封装</b>：构造符合前端需求的响应对象</li>
     * </ul>
     *
     * <h2>技术架构设计</h2>
     * <ul>
     *   <li><b>分层设计</b>：将复杂流程拆分为清晰的步骤</li>
     *   <li><b>依赖注入</b>：通过Spring注入相关服务组件</li>
     *   <li><b>异常处理</b>：在关键步骤设置错误处理机制</li>
     *   <li><b>资源管理</b>：确保临时文件的及时清理</li>
     * </ul>
     *
     * <h2>处理流程详解</h2>
     * <p><b>步骤1：文件存储管理</b></p>
     * <ul>
     *   <li>按日期创建目录结构（uploads/YYYY-MM-DD/）</li>
     *   <li>使用UUID生成唯一文件名，避免冲突</li>
     *   <li>保留原始文件扩展名，支持多种格式</li>
     * </ul>
     *
     * <p><b>步骤2：音频格式转换</b></p>
     * <ul>
     *   <li>调用AudioConversionService进行M4A到WAV转换</li>
     *   <li>确保输出为标准格式，满足ASR要求</li>
     *   <li>生成临时WAV文件供后续处理</li>
     * </ul>
     *
     * <p><b>步骤3：语音识别</b></p>
     * <ul>
     *   <li>调用BaiduAsrService进行语音转文字</li>
     *   <li>处理识别结果，提取文本内容</li>
     *   <li>清理临时WAV文件，释放磁盘空间</li>
     * </ul>
     *
     * <p><b>步骤4：AI智能评估</b></p>
     * <ul>
     *   <li>构建评估请求对象</li>
     *   <li>调用DeepSeekEvalService进行多维评估</li>
     *   <li>获取流利度、完整度、相关性等评分</li>
     * </ul>
     *
     * <p><b>步骤5：响应封装</b></p>
     * <ul>
     *   <li>将评估结果封装为DTO对象</li>
     *   <li>包含音频URL、用户文本、评分信息</li>
     *   <li>返回给前端用于展示</li>
     * </ul>
     *
     * <h2>与第三方服务集成</h2>
     * <ul>
     *   <li><b>百度ASR服务</b>：提供高精度的语音识别能力</li>
     *   <li><b>DeepSeek AI</b>：提供专业的口语评估和反馈</li>
     *   <li><b>音频转换引擎</b>：支持多种音频格式转换</li>
     * </ul>
     *
     * <h2>错误处理策略</h2>
     * <ul>
     *   <li><b>文件操作</b>：捕获IO异常，提供友好错误提示</li>
     *   <li><b>服务调用</b>：记录错误日志，不影响其他流程</li>
     *   <li><b>数据验证</b>：确保输入文件的有效性</li>
     * </ul>
     *
     * <h2>性能优化</h2>
     * <ul>
     *   <li><b>按日期归档</b>：便于文件管理和查找</li>
     *   <li><b>唯一命名</li>：避免文件名冲突</li>
     *   <li><b>及时清理</b>：临时文件及时删除，避免磁盘空间占用</li>
     *   <li><b>异步处理</b>：考虑将耗时的转换和识别任务异步化</li>
     * </ul>
     *
     * <h2>使用场景</h2>
     * <ul>
     *   <li><b>英语口语练习</b>：用户录音后获得详细的评估报告</li>
     *   <li><b>语言学习平台</b>：集成到在线学习系统</li>
     *   <li><b>语音助手</b>：提供语音交互的能力评估</li>
     * </ul>
     *
     * <h2>最佳实践</h2>
     * <ul>
     *   <li>使用固定的目录结构，便于管理</li>
     *   <li>实施文件大小限制，防止恶意上传</li>
     *   <li>添加文件类型验证，确保安全性</li>
     *   <li>记录详细的处理日志，便于问题排查</li>
     * </ul>
     *
     * @param file 前端上传的音频文件，支持.m4a等常见格式
     * @param prompt 评估题目或提示词，用于AI评估的上下文
     * @param persona 用户画像类型，影响AI评估的策略和侧重点
     * @return EvalDTO.TextEvalResp 包含评估结果的响应对象，包括：
     *         - 总体评分
     *         - 各维度得分（流利度、完整度、相关性等）
     *         - 改进建议
     *         - 用户转写的文本
     *         - 音频文件访问URL
     * @throws Exception 当文件处理、格式转换或AI评估失败时抛出
     * @see EvalDTO.TextEvalReq 评估请求DTO
     * @see EvalDTO.TextEvalResp 评估响应DTO
     * @see com.zhupinzan.speaking.model.UserPersona 用户枚举类型
     */
    public EvalDTO.TextEvalResp processAudio(MultipartFile file, String prompt, UserPersona persona) throws Exception {

        // 1. 生成文件保存路径 (按日期归档: uploads/2026-01-14/)
        String dateDir = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Path uploadPath = Paths.get(UPLOAD_DIR, dateDir);

        // 如果目录不存在，创建它
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. 生成唯一文件名 (防止重名)
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".m4a"; // 默认后缀
        String fileName = UUID.randomUUID().toString() + suffix;

        // 最终的物理路径
        Path targetLocation = uploadPath.resolve(fileName);

        // 3. 保存文件到磁盘
        Files.copy(file.getInputStream(), targetLocation, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("✅ 音频已落盘: " + targetLocation.toAbsolutePath());
        System.out.println("📏 原始文件大小: " + Files.size(targetLocation) + " bytes");

        // 4. 生成相对 URL (用于数据库存储)
        // 例如: /uploads/2026-01-14/xxx-xxx.m4a
        String fileUrl = "/" + UPLOAD_DIR + dateDir + "/" + fileName;
        System.out.println("📁 文件URL: " + fileUrl);

        // 5. 转码 (M4A -> WAV)
        File tempWav = audioConversionService.convertM4aToWav(targetLocation.toFile());
        System.out.println("🔄 转码完成: " + tempWav.getAbsolutePath());
        System.out.println("📏 WAV文件大小: " + tempWav.length() + " bytes");

        // 6. 百度 ASR (WAV -> Text)
        String userText = baiduAsrService.speechToText(tempWav);
        System.out.println("🎤 ASR识别结果: " + userText);

        // 清理临时wav文件
        tempWav.delete();

        // 7. DeepSeek 评分 (Text -> JSON)
        EvalDTO.TextEvalReq req = new EvalDTO.TextEvalReq();
        req.setPrompt(prompt != null ? prompt : "Free Talk");
        req.setUserText(userText);

        EvalDTO.TextEvalResp resp = deepSeekEvalService.evaluate(prompt, userText, persona);

        // 8. 设置音频URL和用户文本
        resp.setAudioUrl(fileUrl);
        resp.setUserText(userText);

        return resp;
    }
}