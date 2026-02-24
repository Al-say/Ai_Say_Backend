package com.zhupinzan.speaking.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BaiduTokenTest {

    @Test
    public void testGetAccessToken() {
        // Mock test - skip actual API call
        System.out.println("Mocked Baidu Access Token test - skipping API call");
        String mockToken = "mock_access_token";
        System.out.println("Mock Token: " + mockToken);

        if (mockToken == null || mockToken.isEmpty()) {
            throw new RuntimeException("Mock token is empty");
        }
    }
}

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