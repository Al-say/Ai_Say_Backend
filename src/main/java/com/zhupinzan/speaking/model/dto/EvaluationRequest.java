package com.zhupinzan.speaking.model.dto;

import lombok.Data;

@Data
public class EvaluationRequest {
    private String persona;
    private String scene;
    private String transcript;

    public String getPersona() {
        return persona;
    }

    public String getScene() {
        return scene;
    }

    public String getTranscript() {
        return transcript;
    }
}
