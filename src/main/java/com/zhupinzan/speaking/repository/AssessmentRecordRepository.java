package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {

    // 1. Projection 接口：只查列表需要的字段 (极小 Payload)
    interface GrowthHistoryView {
        Long getId();
        OffsetDateTime getCreatedAt();
        Double getOverallScore();
        Double getFluency();
        Double getCompleteness();
        Double getRelevance();
        String getPrompt(); // 列表页可能需要显示题目
    }

    // 2. 列表查询：返回 Projection
    List<GrowthHistoryView> findByDeviceIdAndPersona(
            String deviceId, UserPersona persona, Pageable pageable
    );

    // 3. 聚合查询：直接算好雷达图的均值 (Growth Analysis)
    // 这里的 JPQL 利用了扁平列，速度极快
    @Query("""
        SELECT
            AVG(r.fluency) as avgFluency,
            AVG(r.completeness) as avgCompleteness,
            AVG(r.relevance) as avgRelevance,
            COUNT(r) as totalCount
        FROM AssessmentRecord r
        WHERE r.deviceId = :deviceId AND r.persona = :persona
          AND r.createdAt >= :from
    """)
    RadarStatsDTO findRadarStats(@Param("deviceId") String deviceId, @Param("persona") UserPersona persona, @Param("from") OffsetDateTime from);

    // DTO 接口定义 (也可以写成 Class)
    interface RadarStatsDTO {
        Double getAvgFluency();
        Double getAvgCompleteness();
        Double getAvgRelevance();
        Long getTotalCount();
    }

    // 兼容旧接口：获取某设备的所有记录 (用于 Profile 总统计)
    List<AssessmentRecord> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
}