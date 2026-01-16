package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/growth")
public class GrowthController {

    @Autowired
    private AssessmentRecordRepository repo;

    /**
     * 1. 获取历史列表 (趋势图数据源)
     * 特点：轻量级，不返回大字段
     */
    @GetMapping("/history")
    public ResponseEntity<List<AssessmentRecordRepository.GrowthHistoryView>> getHistory(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona,
            @RequestParam(defaultValue = "50") int limit
    ) {
        // 安全限制，防止一次拉太多
        int safeLimit = Math.max(1, Math.min(limit, 200));

        var pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(repo.findByDeviceIdAndPersona(deviceId, persona, pageable));
    }

    /**
     * 2. 获取雷达图分析
     * 特点：聚合最近 90 天的数据
     */
    @GetMapping("/analysis")
    public ResponseEntity<AssessmentRecordRepository.RadarStatsDTO> getAnalysis(
            @RequestParam String deviceId,
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona
    ) {
        // 只看最近 90 天
        var fromDate = OffsetDateTime.now().minusDays(90);
        return ResponseEntity.ok(repo.findRadarStats(deviceId, persona, fromDate));
    }

    /**
     * 3. 获取单条详情
     * 特点：全量数据，点击列表项后调用
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<AssessmentRecord> getDetail(
            @PathVariable Long id,
            @RequestParam String deviceId
    ) {
        return repo.findByIdAndDeviceId(id, deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}