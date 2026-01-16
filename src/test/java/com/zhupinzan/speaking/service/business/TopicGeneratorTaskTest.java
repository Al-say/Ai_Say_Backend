package com.zhupinzan.speaking.service.business;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import com.zhupinzan.speaking.repository.DailyTopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
class TopicGeneratorTaskTest {

    @Autowired
    private DailyTopicRepository topicRepository;

    @Test
    void shouldSaveAndRetrieveDailyTopic() {
        // Given
        LocalDate today = LocalDate.now();
        DailyTopic topic = new DailyTopic();
        topic.setTitle("Test Topic");
        topic.setDescription("Test Description");
        topic.setTargetPersona(UserPersona.EXAM_PREP);
        topic.setForDate(today);
        topic.setAiSuggestions("{\"title\":\"Test\",\"description\":\"Test\"}");

        // When
        DailyTopic saved = topicRepository.save(topic);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test Topic");
        assertThat(saved.getTargetPersona()).isEqualTo(UserPersona.EXAM_PREP);

        // When - retrieve
        Optional<DailyTopic> retrieved = topicRepository.findByTargetPersonaAndForDate(UserPersona.EXAM_PREP, today);

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Test Topic");
    }

    @Test
    void shouldDeleteOldTopics() {
        // Given
        LocalDate oldDate = LocalDate.now().minusDays(10);
        DailyTopic oldTopic = new DailyTopic();
        oldTopic.setTitle("Old Topic");
        oldTopic.setDescription("Old Description");
        oldTopic.setTargetPersona(UserPersona.EXAM_PREP);
        oldTopic.setForDate(oldDate);
        oldTopic.setAiSuggestions("old");
        topicRepository.save(oldTopic);

        // When
        long deletedCount = topicRepository.deleteByForDateBefore(LocalDate.now().minusDays(7));

        // Then
        assertThat(deletedCount).isEqualTo(1);
        Optional<DailyTopic> shouldBeDeleted = topicRepository.findByTargetPersonaAndForDate(UserPersona.EXAM_PREP, oldDate);
        assertThat(shouldBeDeleted).isEmpty();
    }
}