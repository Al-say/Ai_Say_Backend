package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.AssessmentResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class DeepSeekService {

    public AssessmentResult evaluateText(String text, String scenarioName) throws IOException {
        // TODO: 实现调用DeepSeek API的逻辑
        // 这里是占位符实现，返回模拟数据

        AssessmentResult result = new AssessmentResult();
        result.setTotalScore(BigDecimal.valueOf(75.5)); // 模拟总分

        AssessmentResult.Dimensions dims = new AssessmentResult.Dimensions();
        dims.setGrammar(BigDecimal.valueOf(70.0));
        dims.setVocabulary(BigDecimal.valueOf(80.0));
        dims.setLogic(BigDecimal.valueOf(75.0));
        result.setDimensions(dims);

        // 模拟语法错误
        List<String> errors = Arrays.asList("I want buy -> I want to buy", "a apple -> an apple");
        result.setGrammarErrors(errors);

        result.setSuggestions("Your text is mostly clear, but pay attention to articles and verb forms.");
        result.setImprovedText("I want to buy an apple.");

        return result;
    }
}