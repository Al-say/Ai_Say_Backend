package com.zhupinzan.speaking.service.business;

import com.zhupinzan.speaking.model.UserPersona;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SceneService {

    public List<String> getRecommendedPrompts(UserPersona persona) {
        if (persona == UserPersona.EXAM_PREP) {
            return List.of(
                "Describe a historical building you visited.", // 雅思 Part 2
                "Do you think AI will replace teachers?"      // 雅思 Part 3
            );
        } else {
            return List.of(
                "Tell me about a time you handled a conflict.", // 面试题
                "How would you ask your boss for a deadline extension?" // 职场沟通
            );
        }
    }
}