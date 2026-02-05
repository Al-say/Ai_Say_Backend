package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.LoginType;
import com.zhupinzan.speaking.model.entity.LoginHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 登录历史记录Repository
 */
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    interface LoginHistoryView {
        Long getId();
        OffsetDateTime getLoginAt();
        LoginType getLoginType();
        String getDeviceId();
    }

    List<LoginHistoryView> findByUserId(Long userId, Pageable pageable);
}
