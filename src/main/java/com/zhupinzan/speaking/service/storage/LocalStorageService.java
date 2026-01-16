package com.zhupinzan.speaking.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 本地文件存储服务 - MinIO 不可用时的备用方案
 */
@Service
@Primary
@Slf4j
public class LocalStorageService {

    private final Path storagePath;
    private final String publicBaseUrl;

    public LocalStorageService(
            @Value("${storage.local-path:./uploads}") String localPath,
            @Value("${storage.public-base-url:http://localhost:8082/files}") String publicBaseUrl) {
        this.storagePath = Paths.get(localPath);
        this.publicBaseUrl = publicBaseUrl;

        try {
            Files.createDirectories(storagePath);
            log.info("📁 Local storage initialized at: {}", storagePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create storage directory", e);
        }
    }

    /**
     * 上传字节数组到本地文件系统
     * 
     * @return 返回可供前端访问的完整 URL
     */
    public String uploadAudio(byte[] data, String deviceId, String extension) {
        try {
            // 生成规范路径: audio/2026-01-16/device_uuid/file_uuid.wav
            String relativePath = String.format("audio/%s/%s/%s%s",
                    LocalDate.now(), deviceId, UUID.randomUUID(), extension);

            Path fullPath = storagePath.resolve(relativePath);
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, data);

            log.info("✅ Audio saved to: {}", fullPath);

            // 拼接访问地址
            return publicBaseUrl.endsWith("/") ? publicBaseUrl + relativePath : publicBaseUrl + "/" + relativePath;
        } catch (IOException e) {
            log.error("Failed to save audio file", e);
            throw new RuntimeException("Failed to save audio", e);
        }
    }
}
