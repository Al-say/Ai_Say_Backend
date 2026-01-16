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

    private final String ffmpegPath;
    private final Path tempDir;
    private final int sampleRate;
    private final int channels;

    public AudioTranscodeService(
            @Value("${ffmpeg.path}") String ffmpegPath,
            @Value("${audio.temp-dir}") String tempDir,
            @Value("${audio.target.sample-rate:16000}") int sampleRate,
            @Value("${audio.target.channels:1}") int channels) {
        this.ffmpegPath = ffmpegPath;
        this.tempDir = Paths.get(tempDir);
        this.sampleRate = sampleRate;
        this.channels = channels;
    }

    /**
     * 转码为 WAV(PCM_s16le) + 16kHz + mono
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

    public record TranscodedAudio(byte[] bytes, String contentType, String extension) {
    }

    public static class AudioTranscodeException extends RuntimeException {
        public AudioTranscodeException(String msg) {
            super(msg);
        }

        public AudioTranscodeException(String msg, Throwable t) {
            super(msg, t);
        }
    }
}