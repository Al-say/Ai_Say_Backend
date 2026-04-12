package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.service.audio.AudioMetaService;
import com.zhupinzan.speaking.service.audio.AudioTranscodeService;
import com.zhupinzan.speaking.service.storage.StorageService;
import com.zhupinzan.speaking.service.AuthUserService;
import com.zhupinzan.speaking.util.CurrentUser;
import com.zhupinzan.speaking.util.CurrentUserInfo;
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
    private final StorageService storage;
    private final AuthUserService authUserService;

    public AudioController(AudioTranscodeService transcodeService, AudioMetaService metaService, StorageService storage,
                           AuthUserService authUserService) {
        this.transcodeService = transcodeService;
        this.metaService = metaService;
        this.storage = storage;
        this.authUserService = authUserService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@CurrentUser CurrentUserInfo user, @RequestPart("file") MultipartFile file) {
        var deviceId = authUserService.requireDeviceId(user);
        try {
            return ResponseEntity.ok(doProcessAndUpload(deviceId, file));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (AudioTranscodeService.AudioTranscodeException e) {
            // 交给统一异常处理映射为 AUDIO_TRANSCODE_ERROR
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    // 内部方法，用于 EvalOrchestrator 调用
    public Map<String, Object> uploadInternal(String deviceId, MultipartFile file) {
        try {
            return doProcessAndUpload(deviceId, file);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    /**
     * 公共上传处理逻辑：校验 → 转码 → 计算时长 → 存储。
     */
    private Map<String, Object> doProcessAndUpload(String deviceId, MultipartFile file) throws Exception {
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
    }

    private String guessExt(String filename) {
        if (filename == null) return ".bin";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return ".bin";
        return filename.substring(idx);
    }
}
