package com.zhupinzan.speaking.model;

public enum UserPersona {
    // 卷卷：备考党 (雅思/托福)
    EXAM_PREP("Exam Candidate", "Strict & Academic"),
    
    // 阿强：职场人 (商务英语)
    CAREER_GROWTH("Business Professional", "Pragmatic & Concise");

    public final String role;
    public final String style;

    UserPersona(String role, String style) {
        this.role = role;
        this.style = style;
    }
}