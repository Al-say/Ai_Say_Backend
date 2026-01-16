package com.zhupinzan.speaking.model.dto;

import com.zhupinzan.speaking.model.UserPersona;

import java.time.LocalDate;
import java.util.Map;

public record DailyTopicDTO(
    LocalDate date,
    UserPersona persona,
    String title,
    String prompt,
    String imageUrl,
    Map<String,Object> payload
) {}