package com.zhupinzan.speaking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @GetMapping
    public ResponseEntity<?> getHome() {
        return ResponseEntity.ok("主页模块 - 欢迎使用AI说话应用");
    }
}