package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 每日挑战题目数据访问层
 */
@Repository
public interface DailyTopicRepository extends JpaRepository<DailyTopic, Long> {

    /**
     * 根据用户画像和日期查找题目
     * @param persona 用户画像
     * @param date 日期
     * @return 题目（如果存在）
     */
    Optional<DailyTopic> findByTargetPersonaAndTopicDate(UserPersona persona, LocalDate date);

    /**
     * 删除指定日期之前的所有题目（清理历史数据）
     * @param date 截止日期
     * @return 删除的记录数
     */
    long deleteByTopicDateBefore(LocalDate date);

    // 新增方法
    Optional<DailyTopic> findByTopicDateAndPersona(LocalDate topicDate, String persona);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO daily_topics(for_date, persona, title, prompt, image_url, payload, created_at)
        VALUES (:date, :persona, :title, :prompt, :imageUrl, CAST(:payload AS jsonb), CURRENT_TIMESTAMP)
        ON CONFLICT (for_date, persona)
        DO UPDATE SET
          title = EXCLUDED.title,
          prompt = EXCLUDED.prompt,
          image_url = EXCLUDED.image_url,
          payload = EXCLUDED.payload
        """, nativeQuery = true)
    void upsertDailyTopic(
        @Param("date") LocalDate date,
        @Param("persona") String persona,
        @Param("title") String title,
        @Param("prompt") String prompt,
        @Param("imageUrl") String imageUrl,
        @Param("payload") String payloadJson
    );
}