package com.zhupinzan.speaking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "baidu")
@Component
@Data
public class BaiduConfig {
    private String appId;
    private String apiKey;
    private String secretKey;
}