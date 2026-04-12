package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import com.zhupinzan.speaking.repository.DailyTopicRepository;
import com.zhupinzan.speaking.service.business.TopicGeneratorTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyChallengeService 单元测试")
class DailyChallengeServiceTest {

    @Mock
    private DailyTopicRepository dailyTopicRepository;

    @Mock
    private TopicGeneratorTask topicGeneratorTask;

    @InjectMocks
    private DailyChallengeService dailyChallengeService;

    private LocalDate today;
    private UserPersona persona;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        persona = UserPersona.EXAM_PREP;
    }

    @Test
    @DisplayName("当缓存中存在主题时，应直接返回缓存的主题")
    void getOrCreate_shouldReturnCachedTopic_whenTopicExists() {
        // Arrange: 准备数据和模拟行为
        DailyTopic cachedTopic = new DailyTopic();
        cachedTopic.setTitle("Cached Topic");
        cachedTopic.setTopicDate(today);
        cachedTopic.setPersona(persona.name());

        when(dailyTopicRepository.findByTopicDateAndPersona(today, persona.name()))
            .thenReturn(Optional.of(cachedTopic));

        // Act: 执行被测试的方法
        DailyTopic result = dailyChallengeService.getOrCreate(today, persona);

        // Assert: 验证结果和交互
        assertNotNull(result);
        assertEquals("Cached Topic", result.getTitle());
        // 验证 topicGeneratorTask 的 generateFor 方法没有被调用
        verify(topicGeneratorTask, never()).generateFor(any(LocalDate.class), any(UserPersona.class));
        // 验证 repository 的 save 方法没有被调用
        verify(dailyTopicRepository, never()).save(any(DailyTopic.class));
    }

    @Test
    @DisplayName("当缓存不存在时，应调用生成器并保存新主题")
    void getOrCreate_shouldCallGeneratorAndSave_whenCacheMiss() throws Exception {
        // Arrange
        DailyTopic generatedTopic = new DailyTopic();
        generatedTopic.setTitle("Generated Topic");
        generatedTopic.setTopicDate(today);
        generatedTopic.setPersona(persona.name());
        generatedTopic.setPayload(Map.of("source", "ai_generated"));

        // 第一次调用返回空，表示缓存未命中
        when(dailyTopicRepository.findByTopicDateAndPersona(today, persona.name()))
            .thenReturn(Optional.empty());

        when(topicGeneratorTask.generateFor(today, persona)).thenReturn(generatedTopic);
        when(dailyTopicRepository.save(any(DailyTopic.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DailyTopic result = dailyChallengeService.getOrCreate(today, persona);

        // Assert
        assertNotNull(result);
        assertEquals("Generated Topic", result.getTitle());
        // 验证生成器被调用了一次
        verify(topicGeneratorTask, times(1)).generateFor(today, persona);
        // 验证save方法被调用了一次
        verify(dailyTopicRepository, times(1)).save(any(DailyTopic.class));
    }

    @Test
    @DisplayName("当生成器失败时，应返回兜底主题")
    void getOrCreate_shouldReturnFallback_whenGeneratorFails() throws Exception {
        // Arrange
        // 模拟生成器抛出异常
        when(topicGeneratorTask.generateFor(today, persona)).thenThrow(new RuntimeException("AI service unavailable"));

        when(dailyTopicRepository.findByTopicDateAndPersona(today, persona.name()))
            .thenReturn(Optional.empty()); // 第一次缓存未命中
        when(dailyTopicRepository.save(any(DailyTopic.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DailyTopic result = dailyChallengeService.getOrCreate(today, persona);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTitle().contains("(兜底)"));
        assertEquals("static_fallback", result.getPayload().get("source"));
        // 验证生成器被调用了一次
        verify(topicGeneratorTask, times(1)).generateFor(today, persona);
        // 验证save方法仍然被调用，保存的是兜底主题
        verify(dailyTopicRepository, times(1)).save(any(DailyTopic.class));
    }
}
