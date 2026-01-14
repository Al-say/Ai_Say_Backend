package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.dto.EvalDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        System.out.println("📁 文件URL: " + fileUrl); // 暂时打印，后续存数据库

        // 5. 模拟 STT (后续换写真实接口)
        String transcribedText = mockSpeechToText(targetLocation);

        // 6. 调用评估逻辑
        EvalDTO.TextEvalReq req = new EvalDTO.TextEvalReq();
        req.setPrompt(prompt != null ? prompt : "Free Talk");
        req.setUserText(transcribedText);
        
        // ⚠️ 记得在 EvalService 里，把 fileUrl 也存进 AssessmentRecord 实体！
        // 目前 EvalService.evaluate 还没接收 fileUrl，你需要微调一下逻辑。
        // 为了简单，我们暂时只跑通上传和评分，不改动 EvalService 接口签名。
        
        return evalService.evaluate(req);
    }

    /**
     * 模拟语音识别 (Mock)
     */
    private String mockSpeechToText(Path audioPath) {
        // 这里只是为了演示，实际上你需要调用 ASR API
        System.out.println("🎤 正在识别音频: " + audioPath);
        
        // 模拟识别结果 (假装用户说了一句英语)
        // 这样前端上传录音后，依然能看到 DeepSeek 的评分，形成闭环。
        return "I love coding and playing basketball.";
    }
}