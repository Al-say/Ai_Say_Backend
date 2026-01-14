package com.zhupinzan.speaking.service;

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
     * 处理音频上传并评估
     * @param file 前端上传的 .m4a 文件
     * @param prompt 题目
     * @return 评估结果
     */
    public EvalDTO.TextEvalResp processAudio(MultipartFile file, String prompt) throws Exception {

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

        // 4. 生成相对 URL (用于数据库存储)
        // 例如: /uploads/2026-01-14/xxx-xxx.m4a
        String fileUrl = "/" + UPLOAD_DIR + dateDir + "/" + fileName;
        System.out.println("📁 文件URL: " + fileUrl);

        // 5. 转码 (M4A -> WAV)
        File tempWav = audioConversionService.convertM4aToWav(targetLocation.toFile());
        System.out.println("🔄 转码完成: " + tempWav.getAbsolutePath());

        // 6. 百度 ASR (WAV -> Text)
        String userText = baiduAsrService.speechToText(tempWav);
        System.out.println("🎤 ASR识别结果: " + userText);

        // 清理临时wav文件
        tempWav.delete();

        // 7. DeepSeek 评分 (Text -> JSON)
        EvalDTO.TextEvalReq req = new EvalDTO.TextEvalReq();
        req.setPrompt(prompt != null ? prompt : "Free Talk");
        req.setUserText(userText);

        EvalDTO.TextEvalResp resp = deepSeekEvalService.evaluateText(prompt, userText);

        // 8. 设置音频URL和用户文本
        resp.setAudioUrl(fileUrl);
        resp.setUserText(userText);

        return resp;
    }
}