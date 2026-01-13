package com.zhupinzan.speaking.model;

import java.math.BigDecimal;

public class AssessmentResult {
    private BigDecimal totalScore;
    private Dimensions dimensions;

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

    public static class Dimensions {
        private BigDecimal vocabulary;
        private BigDecimal logic;

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