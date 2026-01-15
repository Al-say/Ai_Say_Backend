package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.dto.EvalDTO;
import com.zhupinzan.speaking.service.AudioService;
import com.zhupinzan.speaking.service.EvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/growth")
@RequiredArgsConstructor
public class EvalController {

    private final EvalService evalService;
    private final AudioService audioService; // 注入新写的 AudioService

    @PostMapping("/text")
    public ResponseEntity<?> evalText(@RequestBody EvalDTO.TextEvalReq req) {
        // 1. 参数校验 (契约：参数不合法返回 400)
        if (req.getPrompt() == null || req.getPrompt().trim().isEmpty() ||
            req.getUserText() == null || req.getUserText().trim().isEmpty()) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EvalDTO.ErrorResp("BAD_REQUEST", "prompt 或 userText 不能为空"));
        }

        try {
            // 2. 正常业务
            EvalDTO.TextEvalResp resp = evalService.evaluate(req);
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
            @RequestParam(value = "prompt", required = false) String prompt) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new EvalDTO.ErrorResp("BAD_REQUEST", "音频文件不能为空"));
        }

        try {
            // 调用业务逻辑
            EvalDTO.TextEvalResp resp = audioService.processAudio(file, prompt);
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EvalDTO.ErrorResp("AUDIO_PROCESS_FAILED", "处理失败: " + e.getMessage()));
        }
    }
}