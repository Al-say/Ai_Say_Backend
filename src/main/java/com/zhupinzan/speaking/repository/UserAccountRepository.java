package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByAppleSub(String appleSub);
}
