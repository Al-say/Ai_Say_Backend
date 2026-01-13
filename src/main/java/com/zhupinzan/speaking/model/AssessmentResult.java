package com.zhupinzan.speaking.model;

import java.math.BigDecimal;
import java.util.List;

public class AssessmentResult {
    private BigDecimal totalScore;
    private Dimensions dimensions;
    private List<String> grammarErrors;
    private String suggestions;
    private String improvedText;

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public List<String> getGrammarErrors() {
        return grammarErrors;
    }

    public void setGrammarErrors(List<String> grammarErrors) {
        this.grammarErrors = grammarErrors;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }

    public String getImprovedText() {
        return improvedText;
    }

    public void setImprovedText(String improvedText) {
        this.improvedText = improvedText;
    }

    public static class Dimensions {
        private BigDecimal grammar;
        private BigDecimal vocabulary;
        private BigDecimal logic;

        public BigDecimal getGrammar() {
            return grammar;
        }

        public void setGrammar(BigDecimal grammar) {
            this.grammar = grammar;
        }

        public BigDecimal getVocabulary() {
            return vocabulary;
        }

        public void setVocabulary(BigDecimal vocabulary) {
            this.vocabulary = vocabulary;
        }

        public BigDecimal getLogic() {
            return logic;
        }

        public void setLogic(BigDecimal logic) {
            this.logic = logic;
        }
    }
}