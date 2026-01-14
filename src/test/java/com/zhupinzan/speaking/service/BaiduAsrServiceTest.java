package com.zhupinzan.speaking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
public class BaiduAsrServiceTest {

    @Autowired
    private BaiduAsrService baiduAsrService;

    @Test
    public void testBaiduAsr() {
        try {
            // 创建一个测试音频文件（这里使用一个简单的wav文件）
            // 注意：实际测试需要一个真实的wav音频文件
            File testWavFile = new File("test.wav");

            if (testWavFile.exists()) {
                System.out.println("Test WAV file exists: " + testWavFile.getAbsolutePath());
                System.out.println("File size: " + testWavFile.length() + " bytes");

                String result = baiduAsrService.speechToText(testWavFile);
                System.out.println("ASR Result: " + result);
            } else {
                System.out.println("Test WAV file not found at: " + testWavFile.getAbsolutePath());
                System.out.println("Please provide a test audio file.");
            }
        } catch (Exception e) {
            System.err.println("ASR Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}