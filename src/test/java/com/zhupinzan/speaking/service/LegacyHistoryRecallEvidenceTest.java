package com.zhupinzan.speaking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhupinzan.speaking.model.TaskStatus;
import com.zhupinzan.speaking.model.entity.EvaluationTask;
import com.zhupinzan.speaking.repository.EvaluationTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:legacy-recall;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@DisplayName("legacy 历史召回率补证")
class LegacyHistoryRecallEvidenceTest {

    @Autowired
    private EvaluationTaskRepository repository;

    private AsyncEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new AsyncEvaluationService(mock(DeepSeekEvalService.class), repository, new ObjectMapper());
        repository.deleteAll();

        // 新模型主数据
        repository.save(task("N1", 1001L, "USER:1001"));
        repository.save(task("M1", 1001L, "legacy@example.com"));

        // legacy 数据（owner_user_id 为空）
        repository.save(task("L1", null, "legacy@example.com"));
        repository.save(task("L2", null, "apple-sub-1"));
        repository.save(task("L3", null, "USER:1001"));

        // 非目标用户数据（用于检验串数据）
        repository.save(task("O1", 2002L, "USER:2002"));
        repository.save(task("O2", null, "other@example.com"));
    }

    @Test
    @DisplayName("应召回目标用户 legacy 历史且不串他人数据")
    void shouldRecallLegacyHistoryWithoutLeakage() {
        Long ownerUserId = 1001L;
        Set<String> legacyOwnerKeys = Set.of("legacy@example.com", "apple-sub-1");

        List<EvaluationTask> history = service.getUserHistory(ownerUserId, legacyOwnerKeys);
        Set<String> returnedIds = history.stream().map(EvaluationTask::getId).collect(Collectors.toSet());

        // 目标集合：新数据 N1/M1 + legacy L1/L2/L3，共 5 条
        assertEquals(5, returnedIds.size(), "历史去重后记录数应为 5");
        assertTrue(returnedIds.containsAll(Set.of("N1", "M1", "L1", "L2", "L3")), "应完整召回目标用户历史");

        // 不应串出其他用户任务
        assertFalse(returnedIds.contains("O1"), "不应包含其他用户 ownerUserId 数据");
        assertFalse(returnedIds.contains("O2"), "不应包含其他用户 legacy identity 数据");

        // legacy 召回率统计（针对 owner_user_id is null 的目标样本）
        Set<String> compatibilityKeys = Set.of("legacy@example.com", "apple-sub-1", "USER:1001");

        long legacyCandidates = repository.findByUserIdentityInOrderByCreatedAtDesc(List.copyOf(compatibilityKeys))
                .stream()
                .filter(t -> t.getOwnerUserId() == null)
                .count();

        long recalledLegacy = history.stream()
                .filter(t -> t.getOwnerUserId() == null)
                .filter(t -> compatibilityKeys.contains(t.getUserIdentity()))
                .count();

        assertEquals(3L, legacyCandidates, "样本内 legacy 候选应为 3 条");
        assertEquals(legacyCandidates, recalledLegacy, "legacy 召回率应为 100%（样本内）");
    }

    private EvaluationTask task(String id, Long ownerUserId, String userIdentity) {
        EvaluationTask task = new EvaluationTask();
        task.setId(id);
        task.setOwnerUserId(ownerUserId);
        task.setUserIdentity(userIdentity);
        task.setPersona("EXAM_PREP");
        task.setScene("legacy-audit-scene");
        task.setTranscript("sample transcript");
        task.setStatus(TaskStatus.COMPLETED);
        task.setProgress(100);
        task.setResult("{}");
        return task;
    }
}
