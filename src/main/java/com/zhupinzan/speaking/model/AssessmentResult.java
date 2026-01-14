package com.zhupinzan.speaking.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评估结果类，用于存储口语评估的总体结果，包括总分、维度评分、语法错误、建议和改进文本
 */
public class AssessmentResult {
    /** 总分 */
    private BigDecimal totalScore;
    /** 维度评分 */
    private Dimensions dimensions;
    /** 语法错误列表 */
    private List<String> grammarErrors;
    /** 建议 */
    private String suggestions;
    /** 改进后的文本 */
    private String improvedText;

    /** 获取总分 */
    public BigDecimal getTotalScore() {
        return totalScore;
    }

    /** 设置总分 */
    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    /** 获取维度评分 */
    public Dimensions getDimensions() {
        return dimensions;
    }

    /** 设置维度评分 */
    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    /** 获取语法错误列表 */
    public List<String> getGrammarErrors() {
        return grammarErrors;
    }

    /** 设置语法错误列表 */
    public void setGrammarErrors(List<String> grammarErrors) {
        this.grammarErrors = grammarErrors;
    }

    /** 获取建议 */
    public String getSuggestions() {
        return suggestions;
    }

    /** 设置建议 */
    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    /** 获取改进后的文本 */
    public String getImprovedText() {
        return improvedText;
    }

    /** 设置改进后的文本 */
    public void setImprovedText(String improvedText) {
        this.improvedText = improvedText;
    }

    /** 维度类，用于存储语法、词汇和逻辑评分 */
    public static class Dimensions {
        /** 语法评分 */
        private BigDecimal grammar;
        /** 词汇评分 */
        private BigDecimal vocabulary;
        /** 逻辑评分 */
        private BigDecimal logic;

        /** 获取语法评分 */
        public BigDecimal getGrammar() {
            return grammar;
        }

        /** 设置语法评分 */
        public void setGrammar(BigDecimal grammar) {
            this.grammar = grammar;
        }

        /** 获取词汇评分 */
        public BigDecimal getVocabulary() {
            return vocabulary;
        }

        /** 设置词汇评分 */
        public void setVocabulary(BigDecimal vocabulary) {
            this.vocabulary = vocabulary;
        }

        /** 获取逻辑评分 */
        public BigDecimal getLogic() {
            return logic;
        }

        /** 设置逻辑评分 */
        public void setLogic(BigDecimal logic) {
            this.logic = logic;
        }
    }
}