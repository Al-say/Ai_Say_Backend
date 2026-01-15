package com.zhupinzan.speaking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    @GetMapping
    public ResponseEntity<?> getExplore() {
        return ResponseEntity.ok("探索模块 - 发现更多学习资源");
    }
}