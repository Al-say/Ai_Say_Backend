package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 百度 ASR 服务，使用 REST API 调用方式
 */
@Service
public class BaiduAsrService {

    @Value("${baidu.api-key}")
    private String apiKey;

    @Value("${baidu.secret-key}")
    private String secretKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    private String accessToken;
    private long tokenExpiryTime;

    /**
     * 获取 Access Token
     */
    private String getAccessToken() throws IOException {
        // 如果token还没过期，直接返回
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType,
            "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey);

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        System.out.println("🔑 正在获取百度Access Token...");
        System.out.println("🔑 API Key: " + apiKey.substring(0, 10) + "...");
        System.out.println("🔑 Secret Key: " + secretKey.substring(0, 10) + "...");

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("🔑 Token响应码: " + response.code());
            System.out.println("🔑 Token响应内容: " + responseBody);

            if (!response.isSuccessful()) {
                System.err.println("❌ 获取Access Token失败，响应码: " + response.code());
                System.err.println("❌ 错误详情: " + responseBody);
                throw new IOException("Failed to get access token: " + response.code() + " - " + responseBody);
            }

            JSONObject jsonResponse = JSONObject.parseObject(responseBody);

            if (jsonResponse.containsKey("error")) {
                String error = jsonResponse.getString("error");
                String errorDesc = jsonResponse.getString("error_description");
                System.err.println("❌ Token API错误: " + error + " - " + errorDesc);
                throw new IOException("Token error: " + error + " - " + errorDesc);
            }

            accessToken = jsonResponse.getString("access_token");
            // token有效期30天，这里设置为25天后过期
            tokenExpiryTime = System.currentTimeMillis() + (25 * 24 * 60 * 60 * 1000L);

            System.out.println("✅ 成功获取百度Access Token");
            return accessToken;
        }
    }

    /**
     * 将 wav 文件转换为文本 - 真实百度ASR API
     */
    public String speechToText(File wavFile) throws IOException {
        System.out.println("🎤 开始调用百度ASR API进行语音识别...");

        // 获取音频文件的base64编码
        byte[] audioBytes = java.nio.file.Files.readAllBytes(wavFile.toPath());
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 构建请求参数
        JSONObject params = new JSONObject();
        params.put("format", "wav");
        params.put("rate", 16000);
        params.put("channel", 1);
        params.put("cuid", "speaking_app_001");
        params.put("token", getAccessToken());
        params.put("speech", audioBase64);
        params.put("len", audioBytes.length);

        // 设置dev_pid为1737（英语）
        params.put("dev_pid", 1737);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, params.toString());

        Request request = new Request.Builder()
                .url("https://vop.baidu.com/server_api")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("❌ ASR请求失败，响应码: " + response.code());
                String errorBody = response.body() != null ? response.body().string() : "null";
                System.err.println("❌ 错误详情: " + errorBody);
                throw new IOException("ASR request failed: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JSONObject jsonResponse = JSONObject.parseObject(responseBody);

            System.out.println("📡 百度ASR响应: " + responseBody);

            if (jsonResponse.containsKey("err_no") && jsonResponse.getIntValue("err_no") == 0) {
                if (jsonResponse.containsKey("result") && !jsonResponse.getJSONArray("result").isEmpty()) {
                    String recognizedText = jsonResponse.getJSONArray("result").getString(0);
                    System.out.println("🎤 ASR识别结果: " + recognizedText);
                    return recognizedText;
                } else {
                    System.err.println("❌ 未识别到语音内容");
                    throw new RuntimeException("No speech recognized");
                }
            } else {
                String errorMsg = jsonResponse.getString("err_msg");
                int errorCode = jsonResponse.getIntValue("err_no");
                System.err.println("❌ 百度ASR报错 [错误码:" + errorCode + "]: " + errorMsg);
                throw new RuntimeException("ASR Error [" + errorCode + "]: " + errorMsg);
            }
        }
    }
}