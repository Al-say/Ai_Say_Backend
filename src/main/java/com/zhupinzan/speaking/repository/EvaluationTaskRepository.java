package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.EvaluationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EvaluationTaskRepository extends JpaRepository<EvaluationTask, String> {
    List<EvaluationTask> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);

    // 🔍 关键查询：找某个用户的所有历史记录，按时间倒序
    List<EvaluationTask> findByUserIdentityOrderByCreatedAtDesc(String userIdentity);

    List<EvaluationTask> findByUserIdentityInOrderByCreatedAtDesc(List<String> userIdentities);
}
