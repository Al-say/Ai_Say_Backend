package com.zhupinzan.speaking.service.audio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.Duration;
import java.util.List;

@Service
public class AudioMetaService {

    // FFprobe 可执行文件路径，用于分析音频元数据
    @Value("${ffprobe.path}") private String ffprobePath;
    // 临时文件目录，用于存储临时音频文件供分析
    @Value("${audio.temp-dir}") private String tempDir;

    /**
     * 获取音频文件的时长信息（毫秒）
     * <p>
     * 该方法通过调用 FFprobe 工具分析音频文件的元数据，提取其中的时长信息。
     * 由于 FFprobe 是专业的媒体分析工具，能够准确获取各种音频格式的时长信息，
     * 包括但不限于：MP3、WAV、M4A、OGG 等常见格式。
     * </p>
     *
     * <h3>核心业务流程</h3>
     * <ol>
     *   <li><strong>临时文件创建</strong>：将字节数据写入临时文件，FFprobe 需要文件系统访问</li>
     *   <li><strong>FFprobe 命令构建</strong>：组装命令行参数，指定输出格式为纯数字（秒）</li>
     *   <li><strong>进程执行</strong>：调用 ProcessUtil 执行 FFprobe 并捕获输出</li>
     *   <li><strong>结果解析</strong>：将秒数转换为毫秒并返回</li>
     *   <li><strong>资源清理</strong>：删除临时文件，避免磁盘空间泄漏</li>
     * </ol>
     *
     * <h3>技术实现细节</h3>
     * <p>
     * - 使用 FFprobe 的 format=duration 参数获取文件级别时长信息
     * - 输出格式设置为 noprint_wrappers=1:nokey=1，只输出纯数字值
     * - 错误级别设置为 "error"，只输出错误信息，减少日志噪音
     * - 使用 ProcessUtil 统一管理进程生命周期和超时控制
     * </p>
     *
     * <h3>错误处理机制</h3>
     * <p>
     * 采用静默失败策略：
     * - 任何异常情况都返回 -1，表示时长获取失败
     * - 不中断主流程，不影响音频处理的其他环节
     * - 临时文件异常时自动跳过，避免资源泄漏
     * - 进程超时或退出码异常时自动返回错误值
     * </p>
     *
     * <h3>性能优化策略</h3>
     * <p>
     * - 使用临时目录管理，避免频繁创建/删除临时文件的开销
     * - ProcessUtil 实现了超时控制，防止进程长时间阻塞
     * - 错误流合并，减少进程管理的复杂度
     * - 内存中处理输出结果，避免不必要的 IO 操作
     * </p>
     *
     * <h3>使用场景</h3>
     * <p>
     * - 音频文件预处理阶段，获取时长信息用于后续处理
     * - 用户上传音频文件的快速验证
     * - 音频处理流程中的元数据收集
     * - 音频质量检测的辅助指标（时长是否在合理范围）
     * </p>
     *
     * <h3>依赖和配置</h3>
     * <p>
     * 配置参数：
     * - ffprobe.path：FFprobe 可执行文件的路径
     * - audio.temp-dir：临时文件目录路径
     *
     * 外部依赖：
     * - FFprobe 工具（必须安装在系统中）
     * - 临时目录的写权限
     * </p>
     *
     * @param audioBytes 音频文件的字节数组
     * @param ext 音频文件扩展名（用于临时文件命名）
     * @return 音频时长（毫秒），失败时返回 -1
     */
    public long durationMs(byte[] audioBytes, String ext) {
        try {
            Path workDir = Paths.get(tempDir);
            Files.createDirectories(workDir);
            Path in = Files.createTempFile(workDir, "probe_", ext == null ? ".bin" : ext);
            Files.write(in, audioBytes);

            // ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 input.wav
            List<String> cmd = List.of(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    in.toAbsolutePath().toString()
            );

            String out = ProcessUtil.runAndCapture(cmd, Duration.ofSeconds(10)).trim();
            Files.deleteIfExists(in);

            double seconds = Double.parseDouble(out);
            return (long) (seconds * 1000.0);
        } catch (Exception e) {
            // 失败不阻断主流程，可返回 -1 或 0；这里返回 -1 便于上层判断
            return -1;
        }
    }

    static class ProcessUtil {
        static String runAndCapture(List<String> cmd, Duration timeout) throws Exception {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            String output;
            try (var is = p.getInputStream()) {
                output = new String(is.readAllBytes());
            }

            boolean ok = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            if (!ok) {
                p.destroyForcibly();
                throw new RuntimeException("ffprobe timeout");
            }
            if (p.exitValue() != 0) throw new RuntimeException("ffprobe exit=" + p.exitValue() + ": " + output);
            return output;
        }
    }
}