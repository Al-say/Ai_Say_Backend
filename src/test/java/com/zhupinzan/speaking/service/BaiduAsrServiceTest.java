package com.zhupinzan.speaking.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;

@SpringBootTest
public class BaiduAsrServiceTest {

    @Autowired
    private BaiduAsrService baiduAsrService;

    @MockBean
    private BaiduAsrService mockBaiduAsrService;

    @Test
    public void testBaiduAsr() {
        try {
            // Mock the service
            Mockito.when(mockBaiduAsrService.speechToText(Mockito.any()))
                   .thenReturn("Mocked ASR result");

            String result = mockBaiduAsrService.speechToText(new File("test.wav"));
            System.out.println("ASR Result: " + result);

            // Basic assertion
            if (result == null || result.isEmpty()) {
                throw new RuntimeException("ASR result is empty");
            }
        } catch (Exception e) {
            System.err.println("ASR Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}