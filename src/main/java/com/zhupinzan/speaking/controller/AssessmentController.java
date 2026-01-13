package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    // 定义请求参数结构 (内部类)
    static class TextSubmitRequest {
        private Long userId;      // 模拟当前登录用户
        private Long scenarioId;  // 场景ID
        private String scenarioName; // 场景名，例如 "Job Interview"
        private String text;      // 用户说的内容

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getScenarioId() {
            return scenarioId;
        }

        public void setScenarioId(Long scenarioId) {
            this.scenarioId = scenarioId;
        }

        public String getScenarioName() {
            return scenarioName;
        }

        public void setScenarioName(String scenarioName) {
            this.scenarioName = scenarioName;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @PostMapping("/text")
    public ResponseEntity<?> submitTextAssessment(@RequestBody TextSubmitRequest request) {
        try {
            // 执行评测逻辑
            AssessmentRecord record = assessmentService.evaluateText(
                request.getUserId(),
                request.getScenarioId(),
                request.getScenarioName(),
                request.getText()
            );
            
            // 返回处理成功的记录
            return ResponseEntity.ok(record);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("评测失败: " + e.getMessage());
        }
    }
}