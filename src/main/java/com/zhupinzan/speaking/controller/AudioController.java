package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.service.storage.ObjectStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    private final ObjectStorageService storage;

    public AudioController(ObjectStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam String deviceId,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
            }

            String ct = (file.getContentType() == null) ? "application/octet-stream" : file.getContentType();
            // 这里先用原始扩展名；生产建议强制转码后统一 .wav/.m4a
            String ext = guessExt(file.getOriginalFilename());

            var obj = storage.upload(file.getBytes(), ct, deviceId, ext);
            return ResponseEntity.ok(Map.of(
                    "url", obj.url(),
                    "key", obj.key(),
                    "bucket", obj.bucket()
            ));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            // 交给 GlobalExceptionHandler -> INTERNAL_ERROR
            throw new RuntimeException("Upload failed", e);
        }
    }

    private String guessExt(String filename) {
        if (filename == null) return ".bin";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return ".bin";
        return filename.substring(idx);
    }
}