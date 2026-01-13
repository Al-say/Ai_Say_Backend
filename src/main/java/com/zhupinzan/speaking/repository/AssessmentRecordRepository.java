package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.entity.AssessmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRecordRepository extends JpaRepository<AssessmentRecord, Long> {
    
    // 自动生成查询方法：查找某用户的所有练习记录，按时间倒序
    List<AssessmentRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
}