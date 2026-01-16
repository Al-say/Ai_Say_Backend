package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.service.audio.AudioMetaService;
import com.zhupinzan.speaking.service.audio.AudioTranscodeService;
import com.zhupinzan.speaking.service.storage.ObjectStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/audio")
@Slf4j
public class AudioController {

    private final AudioTranscodeService transcodeService;
    private final AudioMetaService metaService;
    private final ObjectStorageService storage;

    public AudioController(AudioTranscodeService transcodeService, AudioMetaService metaService, ObjectStorageService storage) {
        this.transcodeService = transcodeService;
        this.metaService = metaService;
        this.storage = storage;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam String deviceId, @RequestPart("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
            }

            String ext = guessExt(file.getOriginalFilename());
            byte[] raw = file.getBytes();

            // 1) 转码为 wav16k mono pcm
            log.info("Starting transcode for device: {}", deviceId);
            var wav = transcodeService.transcodeToWav16kMono(raw, ext);

            // 2) 计算时长（用于 Profile/统计）
            long durationMs = metaService.durationMs(wav.bytes(), wav.extension());

            // 3) 上传转码产物
            String url = storage.uploadAudio(wav.bytes(), deviceId, wav.extension());

            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "durationMs", durationMs,
                    "format", "pcm_wav_16k"
            ));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (AudioTranscodeService.AudioTranscodeException e) {
            // 交给统一异常处理映射为 AUDIO_TRANSCODE_ERROR（见下方补丁）
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private String guessExt(String filename) {
        if (filename == null) return ".bin";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return ".bin";
        return filename.substring(idx);
    }

    // 内部方法，用于 EvalOrchestrator 调用
    public Map<String, Object> uploadInternal(String deviceId, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
            }

            String ext = guessExt(file.getOriginalFilename());
            byte[] raw = file.getBytes();

            // 1) 转码为 wav16k mono pcm
            log.info("Starting transcode for device: {}", deviceId);
            var wav = transcodeService.transcodeToWav16kMono(raw, ext);

            // 2) 计算时长（用于 Profile/统计）
            long durationMs = metaService.durationMs(wav.bytes(), wav.extension());

            // 3) 上传转码产物
            String url = storage.uploadAudio(wav.bytes(), deviceId, wav.extension());

            return Map.of(
                    "url", url,
                    "durationMs", durationMs,
                    "format", "pcm_wav_16k"
            );
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }
}