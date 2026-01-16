package com.zhupinzan.speaking.service.audio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.Duration;
import java.util.List;

@Service
public class AudioMetaService {

    @Value("${ffprobe.path}") private String ffprobePath;
    @Value("${audio.temp-dir}") private String tempDir;

    /**
     * 返回毫秒时长
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