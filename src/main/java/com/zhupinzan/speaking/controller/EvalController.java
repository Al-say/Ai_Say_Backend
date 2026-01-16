package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.UserPersona;
import com.zhupinzan.speaking.model.dto.EvalAudioResp;
import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.service.EvalOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/eval")
@RequiredArgsConstructor
public class EvalController {

    private final EvalOrchestratorService evalOrchestratorService;

    @PostMapping("/text")
    public ResponseEntity<?> evalText(@RequestBody EvalDTO.TextEvalReq req,
                                      @RequestParam(value = "persona", defaultValue = "EXAM_PREP") UserPersona persona) {
        // 1. 参数校验 (契约：参数不合法返回 400)
        if (req.getDeviceId() == null || req.getDeviceId().trim().isEmpty() ||
            req.getPrompt() == null || req.getPrompt().trim().isEmpty() ||
            req.getUserText() == null || req.getUserText().trim().isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EvalDTO.ErrorResp("BAD_REQUEST", "deviceId, prompt 或 userText 不能为空"));
        }

        try {
            // 2. 正常业务 - 这里暂时返回一个简单的响应
            EvalDTO.TextEvalResp resp = new EvalDTO.TextEvalResp();
            resp.setFluency(85.0);
            resp.setCompleteness(80.0);
            resp.setRelevance(90.0);
            resp.setSuggestions(java.util.List.of("评估完成"));
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            // 3. 异常处理 (契约：评估失败返回 500)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("EVAL_FAILED", "评估失败: " + e.getMessage()));
        }
    }

    /**
     * 音频评估接口
     * Content-Type: multipart/form-data
     */
    @PostMapping("/audio")
    public ResponseEntity<?> evalAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "persona", defaultValue = "EXAM_PREP") UserPersona persona) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new EvalDTO.ErrorResp("BAD_REQUEST", "音频文件不能为空"));
        }

        try {
            // 调用业务逻辑 - 这里暂时返回一个简单的响应
            EvalDTO.TextEvalResp resp = new EvalDTO.TextEvalResp();
            resp.setFluency(80.0);
            resp.setCompleteness(75.0);
            resp.setRelevance(85.0);
            resp.setSuggestions(java.util.List.of("发音清晰", "注意语调"));
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("AUDIO_PROCESS_FAILED", "处理失败: " + e.getMessage()));
        }
    }

    /**
     * 完整音频评估流程：上传 → 转码 → ASR → AI 评估 → 落库 → 进度更新
     */
    @PostMapping("/audio/full")
    public ResponseEntity<?> evaluateAudio(
            @RequestParam("deviceId") String deviceId,
            @RequestParam("persona") UserPersona persona,
            @RequestParam("scene") String scene,
            @RequestPart("audio") MultipartFile audioFile) {

        if (audioFile.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new EvalDTO.ErrorResp("BAD_REQUEST", "音频文件不能为空"));
        }

        try {
            EvalAudioResp result = evalOrchestratorService.evaluateAudio(deviceId, persona, scene, audioFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("EVAL_FAILED", "评估失败: " + e.getMessage()));
        }
    }
}