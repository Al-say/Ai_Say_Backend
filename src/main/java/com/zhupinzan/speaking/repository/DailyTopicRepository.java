package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.DailyTopic;
import org.springframework.data.jpa.repository.JpaRepository;
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
    Optional<DailyTopic> findByTargetPersonaAndForDate(UserPersona persona, LocalDate date);

    /**
     * 删除指定日期之前的所有题目（清理历史数据）
     * @param date 截止日期
     * @return 删除的记录数
     */
    long deleteByForDateBefore(LocalDate date);
}