package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.DeepSeekEvalResult;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

// @SpringBootTest 会加载完整的 Spring 上下文，包括数据库连接和 Bean 注入
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class EvalServiceTest {

    @Autowired
    private EvalService evalService;

    @MockBean
    private DeepSeekEvalService deepSeekEvalService;

    @Test
    @DisplayName("集成测试：调用 DeepSeek 并存库")
    void testEvaluateWorkflow() {
        System.out.println("🚀 开始执行后端自测...");

        // Mock DeepSeekEvalService
        DeepSeekEvalResult.Feedback feedback = new DeepSeekEvalResult.Feedback(
            "Good job!", List.of("Fluency"), List.of(), List.of("Keep practicing"), "Improved text"
        );
        DeepSeekEvalResult mockResult = new DeepSeekEvalResult(
            "success", 85, Map.of("fluency", 85, "completeness", 90, "relevance", 88), feedback
        );
        Mockito.when(deepSeekEvalService.evaluate(Mockito.any(), Mockito.anyString(), Mockito.anyString()))
               .thenReturn(mockResult);

        EvalDTO.TextEvalResp mockResp = new EvalDTO.TextEvalResp();
        mockResp.setFluency(85.0);
        mockResp.setCompleteness(90.0);
        mockResp.setRelevance(88.0);
        mockResp.setIssues(List.of());
        mockResp.setSuggestions(List.of("Good job!"));
        mockResp.setRecordId(1L);
        Mockito.when(deepSeekEvalService.mapToTextEvalResp(Mockito.any(), Mockito.anyString()))
               .thenReturn(mockResp);
        System.out.println("🚀 开始执行后端自测...");

        // 1. 模拟前端传来的请求数据
        EvalDTO.TextEvalReq req = new EvalDTO.TextEvalReq();
        req.setDeviceId("test-device-001");
        req.setPrompt("Please describe your favorite food.");
        // 故意写一句带点小错误的英语，测试 AI 纠错能力
        req.setUserText("My favorite food is noodle. I eat it yesterday very happy.");

        try {
            // 2. 调用业务核心方法 (真实调用 AI)
            long startTime = System.currentTimeMillis();
            EvalDTO.TextEvalResp resp = evalService.evaluate(req, UserPersona.EXAM_PREP);
            long endTime = System.currentTimeMillis();

            // 3. 打印结果到控制台 (方便肉眼观察)
            System.out.println("--------------------------------------------------");
            System.out.println("✅ 测试通过！耗时: " + (endTime - startTime) + "ms");
            System.out.println("--------------------------------------------------");
            System.out.println("📝 数据库记录 ID: " + resp.getRecordId());
            System.out.println("📊 评分 - 流利度: " + resp.getFluency());
            System.out.println("📊 评分 - 完整度: " + resp.getCompleteness());
            System.out.println("📊 评分 - 相关性: " + resp.getRelevance());
            System.out.println("🚩 发现语法问题数: " + (resp.getIssues() != null ? resp.getIssues().size() : 0));

            if (resp.getIssues() != null) {
                resp.getIssues().forEach(issue -> {
                    System.out.println("   ❌ 错误点: " + issue.getMessage());
                    System.out.println("   💡 建议修改: " + issue.getReplacements());
                });
            }

            System.out.println("💬 AI 总体建议: " + resp.getSuggestions());
            System.out.println("--------------------------------------------------");

            // 4. 程序化断言 (如果不满足条件，测试会变红)
            // 确保数据库 ID 已经生成
            if (resp.getRecordId() == null) {
                throw new RuntimeException("❌ 测试失败：RecordId 为空，数据库存储可能失败");
            }
            // 确保分数在合法区间
            if (resp.getFluency() == null || resp.getFluency().doubleValue() < 0
                    || resp.getFluency().doubleValue() > 100) {
                throw new RuntimeException("❌ 测试失败：分数超出范围");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 强制让测试失败，并打印异常
            throw new RuntimeException("❌ 测试过程中发生异常: " + e.getMessage());
        }
    }
}