package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.service.DeepSeekEvalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private DeepSeekEvalService deepSeekEvalService;

    @GetMapping("/test")
    public String healthCheck() {
        // 这个字符串将会显示在你的 iPad 模拟器屏幕上
        return "后端握手成功！当前时间：" + java.time.LocalDateTime.now();
    }

    @PostMapping("/test/deepseek")
    public ResponseEntity<?> testDeepSeek(@RequestBody TestRequest req) {
        try {
            var result = deepSeekEvalService.evaluate(
                com.zhupinzan.speaking.model.UserPersona.EXAM_PREP,
                req.getPrompt(),
                req.getUserText()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("DeepSeek测试失败: " + e.getMessage());
        }
    }

    public static class TestRequest {
        private String prompt;
        private String userText;

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getUserText() { return userText; }
        public void setUserText(String userText) { this.userText = userText; }
    }
}