package com.zhupinzan.speaking.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ObjectStorageService {

    private final S3Client s3;
    private final String bucket;
    private final String publicBaseUrl;

    public ObjectStorageService(S3Client s3,
                                @Value("${storage.bucket}") String bucket,
                                @Value("${storage.public-base-url}") String publicBaseUrl) {
        this.s3 = s3;
        this.bucket = bucket;
        this.publicBaseUrl = publicBaseUrl;
    }

    public UploadedObject upload(byte[] bytes, String contentType, String deviceId, String ext) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String key = "audio/" + date + "/" + deviceId + "/" + UUID.randomUUID() + ext;

        var req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3.putObject(req, RequestBody.fromBytes(bytes));

        // 开发阶段：直接拼 public URL
        String url = publicBaseUrl.endsWith("/") ? publicBaseUrl + key : publicBaseUrl + "/" + key;

        return new UploadedObject(bucket, key, url);
    }

    public record UploadedObject(String bucket, String key, String url) {}
}