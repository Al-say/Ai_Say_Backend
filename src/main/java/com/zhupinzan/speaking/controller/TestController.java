package com.zhupinzan.speaking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String healthCheck() {
        // 这个字符串将会显示在你的 iPad 模拟器屏幕上
        return "后端握手成功！当前时间：" + java.time.LocalDateTime.now();
    }
}