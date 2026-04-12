package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.Device;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import org.springframework.transaction.annotation.Transactional;

public class DeviceRepositoryImpl implements DeviceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void upsertTouch(String deviceId) {
        if (isPostgres()) {
            entityManager.createNativeQuery("""
                INSERT INTO device (device_id, created_at, last_seen_at, meta)
                VALUES (:deviceId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CAST('{}' AS jsonb))
                ON CONFLICT (device_id)
                DO UPDATE SET last_seen_at = CURRENT_TIMESTAMP
                """)
                .setParameter("deviceId", deviceId)
                .executeUpdate();
            return;
        }

        Device device = findByDeviceId(deviceId);
        if (device == null) {
            device = new Device();
            device.setDeviceId(deviceId);
        }
        device.setLastSeenAt(OffsetDateTime.now());
        if (device.getId() == null) {
            entityManager.persist(device);
        } else {
            entityManager.merge(device);
        }
    }

    private Device findByDeviceId(String deviceId) {
        try {
            return entityManager.createQuery(
                    "select d from Device d where d.deviceId = :deviceId", Device.class)
                .setParameter("deviceId", deviceId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private boolean isPostgres() {
        return JpaUtils.isPostgres(entityManager);
    }
}
