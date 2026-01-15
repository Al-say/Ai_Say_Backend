package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.model.dto.EvalDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<EvalDTO.ErrorResp> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new EvalDTO.ErrorResp("BAD_REQUEST", e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<EvalDTO.ErrorResp> handleServiceError(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new EvalDTO.ErrorResp(e.getStatusCode().toString(), e.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EvalDTO.ErrorResp> handleUnknown(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EvalDTO.ErrorResp("INTERNAL_ERROR", "系统内部错误: " + e.getMessage()));
    }
}