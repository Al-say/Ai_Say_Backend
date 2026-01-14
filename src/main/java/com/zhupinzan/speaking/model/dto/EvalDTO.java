package com.zhupinzan.speaking.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class EvalDTO {

    // --- 请求体 (Request) ---
    @Data
    public static class TextEvalReq {
        private String prompt;              // 题目
        private String userText;            // 用户回答
        
        // 可选字段，允许为 null
        private List<String> expectedKeywords; 
        private String referenceAnswer;
    }

    // --- 响应体 (Response) ---
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL) // 仅当字段不为null时才返回，减少传输体积
    public static class TextEvalResp {
        // 核心必需字段
        private Double fluency;
        private Double completeness;
        private Double relevance;

        // 可选扩展字段
        private Long recordId;
        private Integer grammarIssueCount;
        private List<Issue> issues;
        private List<String> suggestions;
        private List<String> missingKeywords;
        private String audioUrl;  // 新增：音频文件相对路径
        private String createdAt; // 修改：改为String类型，ISO 8601格式
        private String userText;  // 新增：ASR识别出的用户文本
    }

    // --- 内部对象：问题详情 ---
    @Data
    public static class Issue {
        private Integer offset;
        private Integer length;
        private String message;
        private List<String> replacements;
    }
    
    // --- 错误响应 (Error) ---
    @Data
    public static class ErrorResp {
        private String code;
        private String message;

        public ErrorResp(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}