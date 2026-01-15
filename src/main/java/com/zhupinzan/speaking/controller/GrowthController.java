package com.zhupinzan.speaking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/growth")
public class GrowthController {

    @GetMapping
    public ResponseEntity<?> getGrowth() {
        return ResponseEntity.ok("成长模块 - 提升您的英语口语能力");
    }
}