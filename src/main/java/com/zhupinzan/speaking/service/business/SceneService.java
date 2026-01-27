package com.zhupinzan.speaking.service.business;

import com.zhupinzan.speaking.model.UserPersona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SceneService {

    private final List<String> examPrepPrompts;
    private final List<String> careerGrowthPrompts;

    public SceneService(
            @Value("${scene.prompts.exam-prep}") String examPrepPromptsStr,
            @Value("${scene.prompts.career-growth}") String careerGrowthPromptsStr) {
        this.examPrepPrompts = Arrays.asList(examPrepPromptsStr.split(";"));
        this.careerGrowthPrompts = Arrays.asList(careerGrowthPromptsStr.split(";"));
    }

    public List<String> getRecommendedPrompts(UserPersona persona) {
        if (persona == UserPersona.EXAM_PREP) {
            return examPrepPrompts;
        } else {
            return careerGrowthPrompts;
        }
    }
}