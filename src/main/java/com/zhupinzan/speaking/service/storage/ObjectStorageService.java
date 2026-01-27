package com.zhupinzan.speaking.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "minio")
public class ObjectStorageService implements StorageService {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicBaseUrl;

    public ObjectStorageService(S3Client s3Client,
                                @Value("${storage.bucket}") String bucket,
                                @Value("${storage.public-base-url}") String publicBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
    }

    /**
     * 上传字节数组
     * @return 返回可供前端访问的完整 URL
     */
    @Override
    public String uploadAudio(byte[] data, String deviceId, String extension) {
        // 生成规范路径: audio/2026-01-16/device_uuid/file_uuid.m4a
        String key = String.format("audio/%s/%s/%s%s",
                LocalDate.now(), deviceId, UUID.randomUUID(), extension);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("audio/wav") // 转码后固定为wav
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));

        // 拼接公网访问地址
        return publicBaseUrl.endsWith("/") ? publicBaseUrl + key : publicBaseUrl + "/" + key;
    }
}
