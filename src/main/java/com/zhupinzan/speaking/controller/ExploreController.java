package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.SceneDTO;
import com.zhupinzan.speaking.repository.SceneRepository;
import com.zhupinzan.speaking.service.business.SceneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    private final SceneRepository sceneRepo;
    private final SceneService sceneService;

    // 推荐使用构造器注入
    public ExploreController(SceneRepository sceneRepo, SceneService sceneService) {
        this.sceneRepo = sceneRepo;
        this.sceneService = sceneService;
    }

    /**
     * 获取探索场景列表
     * GET /api/explore/scenes?persona=EXAM_PREP&category=IELTS
     */
    @GetMapping("/scenes")
    public ResponseEntity<Page<SceneDTO>> getScenes(
            @RequestParam(defaultValue = "EXAM_PREP") UserPersona persona,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        var recommendedPrompts = sceneService.getRecommendedPrompts(persona);
        var scenesPage = sceneRepo.findByPersonaAndCategory(persona, category, pageable)
                .map(s -> new SceneDTO(
                        s.getId(),
                        s.getCode(),
                        s.getTitle(),
                        s.getDescription(),
                        s.getCategory(),
                        s.getTargetPersona(),
                        s.getInitialPrompt(),
                        s.getImageUrl(),
                        recommendedPrompts
                ));
        return ResponseEntity.ok(scenesPage);
    }
}