package com.zhupinzan.speaking.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;

@SpringBootTest
public class BaiduAsrServiceTest {

    @MockBean
    private BaiduAsrService mockBaiduAsrService;

    @Test
    public void testBaiduAsr() throws Exception {
        // Mock the service
        Mockito.when(mockBaiduAsrService.speechToText(Mockito.any()))
               .thenReturn("Mocked ASR result");

        String result = mockBaiduAsrService.speechToText(new File("test.wav"));
        System.out.println("ASR Result: " + result);

        if (result == null || result.isEmpty()) {
            throw new RuntimeException("ASR result is empty");
        }
    }
}