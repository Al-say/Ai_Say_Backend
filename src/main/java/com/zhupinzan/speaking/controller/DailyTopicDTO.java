package com.zhupinzan.speaking.controller;

import java.time.LocalDate;
import java.util.Map;

public record DailyTopicDTO(
    LocalDate date,
    String persona,
    String title,
    String prompt,
    String imageUrl,
    Map<String, Object> payload
) {}