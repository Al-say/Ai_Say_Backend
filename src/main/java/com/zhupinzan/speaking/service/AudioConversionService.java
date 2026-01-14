package com.zhupinzan.speaking.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * 音频转码服务，用于将 m4a 格式转换为 wav 格式以兼容百度 ASR
 */
@Service
public class AudioConversionService {

    /**
     * 将 m4a 文件转换为 16kHz, 16bit, 单声道 wav 格式
     * @param sourceFile 源 m4a 文件
     * @return 转换后的 wav 文件
     * @throws IOException 转换失败时抛出
     * @throws InterruptedException FFmpeg 进程中断时抛出
     */
    public File convertM4aToWav(File sourceFile) throws IOException, InterruptedException {
        String sourcePath = sourceFile.getAbsolutePath();
        String targetPath = sourcePath.replace(".m4a", ".wav");

        // 构建 FFmpeg 命令：转为 16000Hz, 单声道, pcm_s16le 编码，并放大音量以提高识别率
        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-y", "-i", sourcePath,
            "-ar", "16000", "-ac", "1",
            "-filter:a", "volume=2.0",
            "-f", "wav", targetPath
        );

        // 重定向错误流以避免死锁
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("FFmpeg conversion failed with exit code: " + exitCode);
        }

        return new File(targetPath);
    }
}