package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.EvaluationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 异步评估任务的 JPA 仓库接口
 * 提供 EvaluationTask 实体的 CRUD 操作
 */
@Repository
public interface EvaluationTaskRepository extends JpaRepository<EvaluationTask, String> {
}
