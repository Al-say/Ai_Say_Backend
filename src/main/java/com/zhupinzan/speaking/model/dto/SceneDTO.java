package com.zhupinzan.speaking.model.dto;

import com.zhupinzan.speaking.model.UserPersona;

public record SceneDTO(
        Long id,
        String code,
        String title,
        String description,
        String category,
        UserPersona targetPersona,
        String initialPrompt,
        String imageUrl
) {}