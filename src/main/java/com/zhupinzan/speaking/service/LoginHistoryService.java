package com.zhupinzan.speaking.service;

import com.zhupinzan.speaking.model.LoginType;
import com.zhupinzan.speaking.model.entity.LoginHistory;
import com.zhupinzan.speaking.model.entity.UserAccount;
import com.zhupinzan.speaking.repository.LoginHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 登录历史记录服务
 */
@Service
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public void recordLogin(UserAccount account, LoginType loginType) {
        if (account == null || account.getId() == null || loginType == null) {
            return;
        }
        LoginHistory history = new LoginHistory();
        history.setUserId(account.getId());
        history.setLoginType(loginType);
        history.setDeviceId(account.getDeviceId());
        try {
            loginHistoryRepository.save(history);
        } catch (Exception e) {
            // 登录历史写入失败不应影响登录流程
            log.warn("login history save failed, userId={}, type={}, err={}",
                    account.getId(), loginType, e.getMessage());
        }
    }

    public List<LoginHistoryRepository.LoginHistoryView> getHistory(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return Collections.emptyList();
        }
        var pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "loginAt"));
        return loginHistoryRepository.findByUserId(userId, pageable);
    }
}
