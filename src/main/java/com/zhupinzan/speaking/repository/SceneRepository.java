package com.zhupinzan.speaking.repository;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {

    @Query("""
        SELECT s
        FROM Scene s
        WHERE s.targetPersona IS NULL OR s.targetPersona = :persona
        ORDER BY s.category ASC, s.id ASC
    """)
    List<Scene> findByPersona(@Param("persona") UserPersona persona);

    @Query("""
        SELECT s FROM Scene s
        WHERE (s.targetPersona IS NULL OR s.targetPersona = :persona)
          AND (:category IS NULL OR s.category = :category)
        ORDER BY s.category ASC, s.id ASC
    """)
    List<Scene> findByPersonaAndCategory(
            @Param("persona") UserPersona persona,
            @Param("category") String category
    );
}