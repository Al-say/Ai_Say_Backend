package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {

    // 1. Projection: 列表页视图 (极度轻量，无 prompt/transcript/metrics)
    interface GrowthHistoryView {
        Long getId();

        OffsetDateTime getCreatedAt();

        Double getOverallScore();

        // 仅返回核心三维分
        Double getFluency();

        Double getCompleteness();

        Double getRelevance();

        // 🔧 新增：返回场景/题目
        String getScene();
    }

    // 2. DTO: 雷达图统计数据
    interface RadarStatsDTO {
        Double getAvgFluency();

        Double getAvgCompleteness();

        Double getAvgRelevance();

        Long getTotalCount();
    }

    // A. 历史列表查询 (返回 Projection)
    // 自动按 deviceId + persona 过滤
    List<GrowthHistoryView> findByDeviceIdAndPersona(
            String deviceId, UserPersona persona, Pageable pageable);

    // B. 单条详情查询 (返回全量 Entity)
    // 增加 deviceId 校验，防止越权查询
    Optional<AssessmentRecord> findByIdAndDeviceId(Long id, String deviceId);

    // C. 雷达图聚合查询 (最近 N 天)
    @Query("""
                SELECT
                    AVG(r.fluency) as avgFluency,
                    AVG(r.completeness) as avgCompleteness,
                    AVG(r.relevance) as avgRelevance,
                    COUNT(r) as totalCount
                FROM AssessmentRecord r
                WHERE r.deviceId = :deviceId
                  AND r.persona = :persona
                  AND r.createdAt >= :from
            """)
    RadarStatsDTO findRadarStats(
            @Param("deviceId") String deviceId,
            @Param("persona") UserPersona persona,
            @Param("from") OffsetDateTime from);

    // 兼容旧接口：获取某设备和画像的评估记录 (用于历史记录)
    List<AssessmentRecord> findByDeviceIdAndPersonaOrderByCreatedAtDesc(String deviceId, UserPersona persona);

    // 兼容旧接口：获取某设备的所有记录 (用于 Profile 总统计)
    List<AssessmentRecord> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
}