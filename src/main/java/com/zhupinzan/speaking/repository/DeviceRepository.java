package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO device (device_id, created_at, last_seen_at, meta)
        VALUES (:deviceId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '{}'::jsonb)
        ON CONFLICT (device_id)
        DO UPDATE SET last_seen_at = CURRENT_TIMESTAMP
        """, nativeQuery = true)
    void upsertTouch(@Param("deviceId") String deviceId);
}