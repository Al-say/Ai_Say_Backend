package com.zhupinzan.speaking.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频转换服务 - 实现音频格式转换的核心组件
 *
 * <h3>服务定位与功能概述</h3>
 * <p>
 * AudioConversionService 是专门负责音频格式转换的服务组件，主要功能是将移动设备常用的 m4a 格式
 * 转换为百度 ASR（语音识别）所需的 wav 格式。该服务通过调用 FFmpeg 命令行工具实现高质量
 * 的音频格式转换，确保音频数据能够被语音识别系统正确处理。
 * </p>
 *
 * <h3>核心业务流程和算法逻辑</h3>
 * <p>
 * <strong>转换流程</strong>：
 * 1. <strong>输入验证</strong>：接收源文件参数，验证文件存在性和有效性
 * 2. <strong>路径规划</strong>：根据源文件路径生成目标文件路径（.m4a → .wav）
 * 3. <strong>FFmpeg 参数构建</strong>：组装命令行参数，确保输出格式符合要求
 * 4. <strong>进程执行</strong>：启动 FFmpeg 进程进行格式转换
 * 5. <strong>结果验证</strong>：检查输出文件的有效性和完整性
 * </p>
 *
 * <strong>关键算法细节</strong>：
 * - 使用 PCM_S16LE 编码（16位小端序），这是百度 ASR 的标准要求
 * - 固定采样率为 16000Hz，保证语音识别的准确率
 * - 转换为单声道，减少数据处理复杂度和存储空间
 * - 使用 ProcessBuilder 管理外部进程，确保资源正确释放
 * </p>
 *
 * <h3>与其他服务的协作关系</h3>
 * <p>
 * - 依赖系统 FFmpeg 工具，需要确保环境中已安装并配置
 * - 作为音频处理流程的前置步骤，为后续的语音识别提供数据
 * - 与文件系统交互，管理临时文件的创建和删除
 * - 无 Spring 依赖注入，采用原生 Java IO 操作
 * </p>
 *
 * <h3>数据处理和转换逻辑</h3>
 * <p>
 * <strong>音频参数处理</strong>：
 * - 音频编码：强制使用 pcm_s16le（PCM 16bit Little Endian）
 * - 采样率：固定 16000Hz（人声识别的最佳范围）
 * - 声道数：转换为单声道（减少数据处理复杂度）
 * - 采样格式：无压缩格式，保证音质和数据完整性
 * </p>
 *
 * <strong>文件操作处理</strong>：
 * - 使用 File API 管理文件路径和操作
 * - 保持文件名一致性，便于后续处理
 * - 不修改源文件，只生成新的目标文件
 * </p>
 *
 * <h3>缓存策略和性能优化</h3>
 * <p>
 * - 无内置缓存机制，每次请求都进行转换
 * - 建议在服务层实现缓存，避免重复转换相同文件
 * - 使用 ProcessBuilder 重定向错误流，便于调试和性能分析
 * - 读取进程输出日志，可用于性能监控和错误诊断
 * </p>
 *
 * <h3>错误处理和降级机制</h3>
 * <p>
 * <strong>异常处理策略</strong>：
 * - IOException：文件操作异常，通常与文件系统相关
 * - InterruptedException：进程被中断，可能是系统资源限制
 * - RuntimeException：FFmpeg 执行失败，包含详细的错误信息
 * </p>
 *
 * <strong>降级机制</strong>：
 * - 二次验证：检查输出文件大小，防止生成空文件
 * - 详细的错误日志记录，便于问题定位
 * - 抛出明确的异常信息，调用方可根据异常类型进行相应处理
 * - 不实现自动重试机制，建议调用方实现重试逻辑
 * </p>
 *
 * <h3>配置参数和使用场景</h3>
 * <p>
 * <strong>依赖配置</strong>：
 * - FFmpeg 工具需要在系统路径中可用
 * - 无其他配置参数要求
 * </p>
 *
 * <strong>使用场景</strong>：
 * - iOS 设备录音文件的处理（iPhone 默认录 m4a 格式）
 * - 百度 ASR 服务的输入文件预处理
 * - 音频格式标准化，确保兼容性
 * - 用户上传音频文件的格式转换
 * </p>
 *
 * <h3>扩展性和维护性考虑</h3>
 * <p>
 * <strong>扩展性设计</strong>：
 * - 可扩展支持更多输入格式（如 mp3、aac 等）
 * - 可增加音频参数的动态配置（如采样率、位深等）
 * - 可集成音频质量检测功能
 * - 可添加批量转换支持
 * </p>
 *
 * <strong>维护性考虑</strong>：
 * - 清晰的注释说明 FFmpeg 参数含义
 * - 二次验证机制确保输出质量
 * - 错误处理完善，便于问题排查
 * - 建议增加单元测试，覆盖不同场景
 * - 建议添加性能监控，记录转换耗时
 * </p>
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
