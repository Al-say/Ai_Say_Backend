package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.service.DailyChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final DailyChallengeService dailyService;

    public HomeController(DailyChallengeService dailyService) {
        this.dailyService = dailyService;
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyTopicDTO> getDaily(
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona) {
        
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        var topic = dailyService.getOrCreate(today, persona);
        
        DailyTopicDTO dto = new DailyTopicDTO(
            topic.getTopicDate(),
            topic.getPersona(),
            topic.getTitle(),
            topic.getPrompt(),
            topic.getImageUrl(),
            topic.getPayload()
        );
        
        return ResponseEntity.ok(dto);
    }
}