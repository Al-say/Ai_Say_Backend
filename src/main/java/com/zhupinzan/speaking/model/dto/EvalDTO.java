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
        // 1. 核心评分 (必须匹配)
        private Double fluency;
        private Double completeness;
        private Double relevance;

        // 2. 详细分析 (必须匹配)
        private List<Issue> issues;
        private List<String> suggestions;

        // 3. 资源链接 (必须匹配)
        private String audioUrl;

        // 4. 补充字段 (后端建议加上，虽然最小契约没写，但前端HistoryDetailView可能需要)
        private String userText; // ASR识别出的文本
        private Long recordId;   // 数据库ID
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