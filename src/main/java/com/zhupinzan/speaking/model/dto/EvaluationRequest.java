package com.zhupinzan.speaking.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EvaluationRequest {

    @NotBlank(message = "Transcript cannot be empty")
    private String transcript;

    private String persona;
    private String scene;
}
