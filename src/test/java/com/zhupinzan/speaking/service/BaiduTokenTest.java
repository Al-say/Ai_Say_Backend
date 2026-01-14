package com.zhupinzan.speaking.service;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class BaiduTokenTest {

    @Value("${baidu.api-key}")
    private String apiKey;

    @Value("${baidu.secret-key}")
    private String secretKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Test
    public void testGetAccessToken() {
        try {
            System.out.println("Testing Baidu Access Token with:");
            System.out.println("API Key: " + apiKey);
            System.out.println("Secret Key: " + secretKey);

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType,
                "grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey);

            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/oauth/2.0/token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body().string();
                System.out.println("Response Code: " + response.code());
                System.out.println("Response Body: " + responseBody);

                if (response.isSuccessful()) {
                    System.out.println("✅ Access Token 获取成功！");
                } else {
                    System.out.println("❌ Access Token 获取失败！");
                }
            }
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}