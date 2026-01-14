package com.zhupinzan.speaking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot主应用类，用于启动口语评估应用
 */
@SpringBootApplication
public class SpeakingApplication {

    /**
     * 主方法，启动Spring Boot应用
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SpeakingApplication.class, args);
    }

}