package com.zhupinzan.speaking.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        File targetFile = new File(targetPath);

        try {
            // 构建 FFmpeg 命令：强制转为 16k采样率、16bit位深(pcm_s16le)、单声道
            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-y");              // 覆盖同名文件
            command.add("-i");
            command.add(sourcePath);
            command.add("-acodec");
            command.add("pcm_s16le");       // 🔴 关键：百度只认 PCM 16bit Little Endian
            command.add("-ar");
            command.add("16000");           // 🔴 关键：采样率 16000
            command.add("-ac");
            command.add("1");               // 🔴 关键：单声道
            command.add(targetPath);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 把错误输出合并，方便调试

            Process process = pb.start();
            
            // 读取 FFmpeg 的输出日志（如果转码失败，这里能看到原因）
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // System.out.println(line); // 调试时可以打印
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 转码失败，退出码: " + exitCode);
            }

            // 🚨 二次检查：生成的文件是否大小为 0？
            if (!targetFile.exists() || targetFile.length() < 100) {
                 throw new RuntimeException("FFmpeg 生成了空文件，请检查音频源是否正常");
            }

            return targetFile;

        } catch (Exception e) {
            throw new RuntimeException("音频格式转换失败: " + e.getMessage(), e);
        }
    }
}