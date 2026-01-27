package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.repository.AssessmentRecordRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class GrowthService {

    private final AssessmentRecordRepository repo;

    public GrowthService(AssessmentRecordRepository repo) {
        this.repo = repo;
    }

    @Cacheable(
        cacheNames = "growthRadar",
        key = "#deviceId + '|' + #persona + '|' + #from.toInstant().toEpochMilli()",
        unless = "#result == null"
    )
    public AssessmentRecordRepository.RadarStatsDTO getRadarStats(
        String deviceId,
        UserPersona persona,
        OffsetDateTime from
    ) {
        return repo.findRadarStats(deviceId, persona, from);
    }
}
