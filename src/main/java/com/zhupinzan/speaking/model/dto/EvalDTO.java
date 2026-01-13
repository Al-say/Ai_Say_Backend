package com.zhupinzan.speaking.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class EvalDTO {

    public static class TextEvalReq {
        private String prompt;
        private String userText;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getUserText() {
            return userText;
        }

        public void setUserText(String userText) {
            this.userText = userText;
        }
    }

    public static class TextEvalResp {
        private Long recordId;
        private BigDecimal fluency;
        private BigDecimal completeness;
        private BigDecimal relevance;
        private Integer grammarIssueCount;
        private List<Issue> issues;
        private String suggestions;

        public Long getRecordId() {
            return recordId;
        }

        public void setRecordId(Long recordId) {
            this.recordId = recordId;
        }

        public BigDecimal getFluency() {
            return fluency;
        }

        public void setFluency(BigDecimal fluency) {
            this.fluency = fluency;
        }

        public BigDecimal getCompleteness() {
            return completeness;
        }

        public void setCompleteness(BigDecimal completeness) {
            this.completeness = completeness;
        }

        public BigDecimal getRelevance() {
            return relevance;
        }

        public void setRelevance(BigDecimal relevance) {
            this.relevance = relevance;
        }

        public Integer getGrammarIssueCount() {
            return grammarIssueCount;
        }

        public void setGrammarIssueCount(Integer grammarIssueCount) {
            this.grammarIssueCount = grammarIssueCount;
        }

        public List<Issue> getIssues() {
            return issues;
        }

        public void setIssues(List<Issue> issues) {
            this.issues = issues;
        }

        public String getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(String suggestions) {
            this.suggestions = suggestions;
        }
    }

    public static class Issue {
        private String message;
        private String replacements;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getReplacements() {
            return replacements;
        }

        public void setReplacements(String replacements) {
            this.replacements = replacements;
        }
    }
}