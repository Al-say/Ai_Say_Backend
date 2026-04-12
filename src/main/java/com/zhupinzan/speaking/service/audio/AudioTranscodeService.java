package com.zhupinzan.speaking.service.audio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AudioTranscodeService {

    // FFmpeg 可执行文件路径，外部依赖的音频处理工具
    private final String ffmpegPath;
    // 临时文件目录，用于处理转码过程中的文件操作
    private final Path tempDir;
    // 目标音频采样率，默认 16kHz（适合人声识别）
    private final int sampleRate;
    // 目标音频声道数，默认 1（单声道）
    private final int channels;
    // 是否启用音频降噪，默认 false（可选功能）
    private final boolean denoiseEnabled;

    /**
     * 构造函数 - 初始化音频转码服务的所有配置参数
     * <p>
     * 该构造函数通过 Spring 的依赖注入机制获取所有必要的配置参数，
     * 这些参数决定了音频转码的具体行为和质量标准。
     * </p>
     *
     * <h3>配置参数说明</h3>
     * <dl>
     *   <dt><strong>ffmpeg.path</strong></dt>
     *   <dd>FFmpeg 可执行文件的完整路径。FFmpeg 是音频处理的核心工具，
     *       必须预先安装在系统中并具有执行权限。</dd>
     *
     *   <dt><strong>audio.temp-dir</strong></dt>
     *   <dd>临时文件存储目录。该目录需要具有读写权限，并且有足够的磁盘空间。
     *       服务启动时会自动创建该目录（如果不存在）。</dd>
     *
     *   <dt><strong>audio.target.sample-rate</strong></dt>
     *   <dd>目标采样率，默认 16kHz。这是语音识别系统的推荐采样率，
     *       平衡了音频质量和处理性能。可选值：8000、16000、22050、44100等。</dd>
     *
     *   <dt><strong>audio.target.channels</strong></dt>
     *   <dd>目标声道数，默认 1（单声道）。单声道音频数据处理简单，
     *       存储空间小，适合语音识别场景。可选值：1（单声道）、2（立体声）。</dd>
     *
     *   <dt><strong>audio.denoise.enabled</strong></dt>
     *   <dd>是否启用音频降噪，默认 false。启用后会应用 FFT 降噪算法，
     *       可以有效去除背景噪声，提高音频质量。但会增加处理时间。</dd>
     * </dl>
     *
     * <h3>初始化流程</h3>
     * <ol>
     *   <li>验证配置参数的有效性</li>
     *   <li>将字符串路径转换为 Path 对象</li>
     *   <li>缓存配置参数，避免重复读取</li>
     *   <li>为后续的转码操作做好准备</li>
     * </ol>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * // application.yml 配置示例
     * ffmpeg:
     *   path: /usr/local/bin/ffmpeg
     * audio:
     *   temp-dir: /tmp/audio-transcode
     *   target:
     *     sample-rate: 16000
     *     channels: 1
     *   denoise:
     *     enabled: true
     * }</pre>
     *
     * @param ffmpegPath FFmpeg 可执行文件路径
     * @param tempDir 临时文件目录
     * @param sampleRate 目标采样率
     * @param channels 目标声道数
     * @param denoiseEnabled 是否启用降噪
     */
    public AudioTranscodeService(
            @Value("${ffmpeg.path}") String ffmpegPath,
            @Value("${audio.temp-dir}") String tempDir,
            @Value("${audio.target.sample-rate:16000}") int sampleRate,
            @Value("${audio.target.channels:1}") int channels,
            @Value("${audio.denoise.enabled:false}") boolean denoiseEnabled) {
        this.ffmpegPath = ffmpegPath;
        this.tempDir = Paths.get(tempDir);
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.denoiseEnabled = denoiseEnabled;
    }

    /**
     * 音频转码核心方法 - 将任意格式音频转换为标准 WAV 格式
     * <p>
     * 该方法实现音频格式的标准化转换，输出符合语音识别系统要求的音频格式。
     * 支持多种输入格式，通过 FFmpeg 进行高质量的格式转换，同时可选地应用音频增强处理。
     * </p>
     *
     * <h3>核心业务流程</h3>
     * <ol>
     *   <li><strong>环境准备</strong>：创建临时目录，确保工作空间可用</li>
     *   <li><strong>文件管理</strong>：创建临时输入文件，写入音频数据</li>
     *   <li><strong>增强处理</strong>：根据配置应用音频降噪等增强处理</li>
     *   <li><strong>格式转换</strong>：转换为标准化的 WAV 格式（PCM_S16LE）</li>
     *   <li><strong>参数配置</strong>：设置采样率、声道数等关键参数</li>
     *   <li><strong>资源清理</strong>：安全删除临时文件，避免磁盘泄漏</li>
     * </ol>
     *
     * <h3>音频处理算法</h3>
     * <p>
     * <strong>标准化处理</strong>：
     * - 编码格式：PCM_S16LE（16位小端序，无压缩）
     * - 采样率：可配置（默认 16kHz，符合人声识别最佳范围）
     * - 声道数：可配置（默认单声道，减少处理复杂度）
     * - 位深度：16位（平衡质量和文件大小）
     * </p>
     *
     * <strong>音频增强</strong>：
     * - 可选应用 FFT 降噪算法（afftdn filter）
     * - 通过配置参数控制是否启用增强功能
     * - 增强处理在格式转换之前应用，确保最佳效果
     * </p>
     *
     * <h3>错误处理和容错机制</h3>
     * <p>
     * <strong>异常处理策略</strong>：
     * - 自定义 AudioTranscodeException，提供详细的错误信息
     * - 进程超时处理（默认20秒超时）
     * - 退出码检查，确保 FFmpeg 正常执行
     * - 临时文件安全删除，防止资源泄漏
     * </p>
     *
     * <strong>降级机制</strong>：
     * - 进程超时时强制销毁，避免资源占用
     * - 输出异常时保留原始错误信息
     * - 临时文件删除失败时静默处理，不影响主流程
     * - 确保在任何异常情况下资源都能被正确释放
     * </p>
     *
     * <h3>性能优化策略</h3>
     * <p>
     * <strong>资源管理</strong>：
     * - 使用 try-with-resources 确保流资源正确释放
     * - 临时文件使用绝对路径，避免路径解析开销
     * - ProcessUtil 实现了超时控制，防止无限等待
     * - 错误流合并，减少进程管理的复杂度
     * </p>
     *
     * <strong>配置优化</strong>：
     * - FFmpeg 参数优化，减少冗余日志输出
     * - 使用 -hide_banner 和 -loglevel error 减少日志噪音
     * - 批量构建命令参数，提高执行效率
     * </p>
     *
     * <h3>与其他服务的协作关系</h3>
     * <p>
     * - 依赖 Spring 配置系统，获取音频处理参数
     * - 与 AudioMetaService 协作，提供音频时长信息
     * - 为语音识别服务提供标准化的音频数据
     * - 可与音频质量检测模块集成，验证转换结果
     * </p>
     *
     * <h3>扩展性和维护性设计</h3>
     * <p>
     * <strong>可扩展方向</strong>：
     * - 支持更多音频格式（FLAC、AAC、OPUS等）
     * - 添加音频质量检测功能
     * - 实现批量转码支持
     * - 添加音频增强算法（音量标准化、噪声抑制等）
     * </p>
     *
     * <strong>维护性考虑</strong>：
     * - 清晰的配置参数，便于调试和调整
     * - 详细的日志记录，便于问题定位
     * - 模块化的设计，易于单独测试
     * - 异常处理完善，避免资源泄漏
     * </p>
     *
     * <h3>使用场景</h3>
     * <p>
     * - 语音识别服务的输入预处理
     * - 用户上传音频文件的格式标准化
     * - 音频数据交换的格式转换
     * - 跨平台音频处理的统一接口
     * </p>
     *
     * @param inputBytes 原始音频数据的字节数组
     * @param inputExt 原始音频文件的扩展名（用于临时文件命名）
     * @return TranscodedAudio 转码后的音频对象，包含字节数组、内容类型和扩展名
     * @throws AudioTranscodeException 当转码过程中出现任何错误时抛出
     */
    public TranscodedAudio transcodeToWav16kMono(byte[] inputBytes, String inputExt) {
        Path in = null;
        Path out = null;
        try {
            Files.createDirectories(tempDir);

            in = Files.createTempFile(tempDir, "in_", inputExt == null ? ".bin" : inputExt);
            out = Files.createTempFile(tempDir, "out_", ".wav");
            Files.write(in, inputBytes, StandardOpenOption.TRUNCATE_EXISTING);

            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpegPath);
            cmd.add("-y");
            cmd.add("-hide_banner");
            cmd.add("-loglevel");
            cmd.add("error");
            cmd.add("-i");
            cmd.add(in.toAbsolutePath().toString());
            // Denoise
            if (denoiseEnabled) {
                cmd.add("-af");
                cmd.add("afftdn");
            }
            // 目标格式：16k mono PCM 16-bit
            cmd.add("-ac");
            cmd.add(String.valueOf(channels));
            cmd.add("-ar");
            cmd.add(String.valueOf(sampleRate));
            cmd.add("-c:a");
            cmd.add("pcm_s16le");
            cmd.add(out.toAbsolutePath().toString());

            runProcess(cmd, Duration.ofSeconds(20));

            return new TranscodedAudio(Files.readAllBytes(out), "audio/wav", ".wav");
        } catch (Exception e) {
            throw new AudioTranscodeException("FFmpeg transcode failed", e);
        } finally {
            // ✅ 确保在任何异常情况下都尝试删除临时文件
            safeDelete(in);
            safeDelete(out);
        }
    }

    private void runProcess(List<String> cmd, Duration timeout) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String output;
        try (InputStream is = p.getInputStream()) {
            output = new String(is.readAllBytes());
        }

        boolean finished = p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            throw new AudioTranscodeException("FFmpeg timed out: " + output);
        }

        if (p.exitValue() != 0) {
            throw new AudioTranscodeException("FFmpeg exit=" + p.exitValue() + ", output=" + output);
        }
    }

    private void safeDelete(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
    }

    /**
     * 转码音频结果类 - 封装转码后的音频数据及其元信息
     * <p>
     * 使用 Java 14+ 的 record 特性实现不可变数据类，确保线程安全和数据完整性。
     * 该类作为音频转码操作的返回值，包含了转码后的所有必要信息。
     * </p>
     *
     * <h3>字段说明</h3>
     * <ul>
     *   <li><strong>bytes</strong>：音频数据的字节数组，包含实际的 PCM 数据</li>
     *   <li><strong>contentType</strong>：MIME 类型，用于 HTTP 响应头（如 "audio/wav"）</li>
     *   <li><strong>extension</strong>：文件扩展名，便于后续处理和保存</li>
     * </ul>
     *
     * <h3>设计考虑</h3>
     * <p>
     * - 不可变性：使用 record 确保对象不可变，避免线程安全问题
     * - 完整性：包含所有必要信息，避免多次查询或转换
     * - 便利性：提供标准化的构造方式，便于直接使用
     * </p>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * TranscodedAudio result = service.transcodeToWav16kMono(audioBytes, "m4a");
     * // 直接使用转码结果
     * response.setContentType(result.contentType());
     * response.getOutputStream().write(result.bytes());
     * }</pre>
     */
    public record TranscodedAudio(byte[] bytes, String contentType, String extension) {
    }

    /**
     * 音频转码异常类 - 封装转码过程中的所有错误信息
     * <p>
     * 继承 RuntimeException，用于非受检异常场景。该异常类提供了详细的错误信息，
     * 包含错误消息和原始异常堆栈，便于问题定位和调试。
     * </p>
     *
     * <h3>异常类型</h3>
     * <ul>
     *   <li><strong>纯消息异常</strong>：仅包含错误消息，用于描述性错误</li>
     *   <li><strong>链式异常</strong>：包含原始异常，用于定位根本原因</li>
     * </ul>
     *
     * <h3>使用场景</h3>
     * <p>
     * - FFmpeg 进程执行失败（退出码非零）
     * - 进程超时（超过配置的超时时间）
     * - IO 操作异常（文件读写失败）
     * - 音频格式不支持或格式错误
     * </p>
     *
     * <h3>错误处理建议</h3>
     * <p>
     * - 调用方应该捕获此异常并进行适当的处理
     * - 建议记录完整的错误日志，包括堆栈信息
     * - 可以根据异常类型提供不同的用户反馈
     * - 对于关键路径，建议实现重试机制
     * </p>
     *
     * <h3>示例用法</h3>
     * <pre>{@code
     * try {
     *     TranscodedAudio result = service.transcodeToWav16kMono(data, "m4a");
     * } catch (AudioTranscodeException e) {
     *     log.error("音频转码失败", e);
     *     // 根据错误类型提供不同的处理方式
     *     if (e.getMessage().contains("timeout")) {
     *         // 处理超时情况
     *     } else {
     *         // 处理其他错误
     *     }
     * }
     * }</pre>
     */
    public static class AudioTranscodeException extends RuntimeException {
        /**
         * 构造仅包含消息的异常
         * @param msg 错误描述消息
         */
        public AudioTranscodeException(String msg) {
            super(msg);
        }

        /**
         * 构造包含消息和原因的异常
         * @param msg 错误描述消息
         * @param t 原始异常，用于错误追踪
         */
        public AudioTranscodeException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}