package com.zhupinzan.speaking.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EvaluationRequest {

    @NotBlank(message = "Transcript cannot be empty")
    private String transcript;

    @NotBlank(message = "Persona cannot be empty")
    private String persona;

    @NotBlank(message = "Scene cannot be empty")
    private String scene;
}
