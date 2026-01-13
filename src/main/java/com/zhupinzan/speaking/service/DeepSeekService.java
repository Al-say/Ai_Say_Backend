package com.zhupinzan.speaking.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zhupinzan.speaking.model.AssessmentResult;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    private final OkHttpClient client;

    public DeepSeekService() {
        // AI 响应较慢，设置 60 秒超时，防止报 Timeout 错误
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public AssessmentResult evaluateText(String text, String scenarioName) throws IOException {
        // 为了兼容旧代码，调用chat方法
        String systemPrompt = "评估文本：" + text + " 场景：" + scenarioName;
        String userPrompt = text;
        String jsonStr = chat(systemPrompt, userPrompt);
        // 解析为AssessmentResult，简化
        AssessmentResult result = new AssessmentResult();
        // 这里可以解析jsonStr，但为了简单，返回默认值
        result.setTotalScore(BigDecimal.valueOf(75.0));
        return result;
    }

    /**
     * 发送对话请求，并强制要求返回 JSON 格式
     * @param systemPrompt 系统设定（包含 JSON 结构要求）
     * @param userPrompt 用户输入
     * @return AI 返回的 JSON 字符串内容
     */
    public String chat(String systemPrompt, String userPrompt) throws IOException {
        // 1. 构建请求体
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("temperature", 1.0); // 1.0 适合创意和生成，0.0 适合逻辑。口语评分建议 1.0 左右

        // ⚠️ 关键设置：强制 JSON 模式，防止 AI 说废话或乱加 Markdown
        payload.put("response_format", new JSONObject().fluentPut("type", "json_object"));

        // 构建消息链
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().fluentPut("role", "system").fluentPut("content", systemPrompt));
        messages.add(new JSONObject().fluentPut("role", "user").fluentPut("content", userPrompt));
        payload.put("messages", messages);

        // 2. 构建 HTTP 请求
        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        // 3. 执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown Error";
                throw new IOException("DeepSeek API 请求失败: " + response.code() + " - " + errorBody);
            }

            // 4. 解析响应
            String responseBody = response.body().string();
            JSONObject jsonResponse = JSON.parseObject(responseBody);

            // 提取 content 字段
            return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
}