package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.service.DailyChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * 主页模块相关的REST控制器。
 * <p>
 * 该控制器是应用主页功能的主要入口点，目前负责提供每日挑战的功能。
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final DailyChallengeService dailyService; // 注入每日挑战服务，负责处理相关业务逻辑

    public HomeController(DailyChallengeService dailyService) {
        this.dailyService = dailyService;
    }

    /**
     * 获取当天的每日挑战题目。
     * <p>
     * 此端点根据用户画像（persona）提供一个每日挑战题目。
     * 它会调用 {@link DailyChallengeService} 来获取或创建当天的题目，
     * 然后将其封装成DTO对象返回给客户端。
     *
     * @param persona 用户画像（例如，备考党 'EXAM_PREP' 或 职场人 'CAREER_GROWTH'），
     *                用于获取符合用户需求的题目。默认为 'EXAM_PREP'。
     * @return 一个包含每日挑战题目信息的 {@link DailyTopicDTO}。
     */
    @GetMapping("/daily")
    public ResponseEntity<DailyTopicDTO> getDaily(
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona) {

        // 获取当前的UTC日期，以保证日期的一致性
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        // 调用服务层获取或创建当天的题目
        var topic = dailyService.getOrCreate(today, persona);

        // 将从服务层获取的实体对象转换为用于网络传输的DTO对象
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