package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/growth")
public class GrowthController {

    private final AssessmentRecordRepository assessmentRecordRepository;

    public GrowthController(AssessmentRecordRepository assessmentRecordRepository) {
        this.assessmentRecordRepository = assessmentRecordRepository;
    }

    /**
     * 获取成长历史记录列表
     * 使用 Projection 优化性能，只返回必要字段
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam String deviceId,
            @RequestParam UserPersona persona,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<AssessmentRecordRepository.GrowthHistoryView> history =
            assessmentRecordRepository.findByDeviceIdAndPersona(deviceId, persona, pageable);

        return ResponseEntity.ok(history);
    }

    /**
     * 获取成长分析数据（雷达图统计）
     * 使用聚合查询，计算最近30天的平均分
     */
    @GetMapping("/analysis")
    public ResponseEntity<?> getAnalysis(
            @RequestParam String deviceId,
            @RequestParam UserPersona persona) {

        // 计算最近30天的统计
        OffsetDateTime from = OffsetDateTime.now().minusDays(30);
        AssessmentRecordRepository.RadarStatsDTO stats =
            assessmentRecordRepository.findRadarStats(deviceId, persona, from);

        return ResponseEntity.ok(stats);
    }

    /**
     * 获取单个评估记录详情
     * 包含完整数据，用于详情页展示
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getDetail(@PathVariable Long id) {
        Optional<AssessmentRecord> record = assessmentRecordRepository.findById(id);

        if (record.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(record.get());
    }
}