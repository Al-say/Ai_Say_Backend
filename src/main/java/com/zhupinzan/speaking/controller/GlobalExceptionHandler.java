package com.zhupinzan.speaking.controller;

import com.zhupinzan.speaking.config.RequestIdFilter;
import com.zhupinzan.speaking.model.ErrorCode;
import com.zhupinzan.speaking.model.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private String getRequestId() {
        return MDC.get(RequestIdFilter.MDC_KEY);
    }

    // 处理 Spring 内置的响应状态异常 (如 404, 403)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException e, HttpServletRequest req) {
        HttpStatus status = (HttpStatus) e.getStatusCode();
        ErrorCode code = switch (status) {
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case FORBIDDEN -> ErrorCode.FORBIDDEN;
            default -> ErrorCode.BAD_REQUEST;
        };
        return buildResponse(status, code, e.getReason(), req);
    }

    // 处理兜底的 500 错误
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Exception e, HttpServletRequest req) {
        log.error("[{}] Unhandled exception: ", getRequestId(), e); // 打印堆栈以便排查
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Internal Server Error", req);
    }

    // 处理存储服务异常
    @ExceptionHandler(software.amazon.awssdk.core.exception.SdkException.class)
    public ResponseEntity<ApiErrorResponse> handleStorageError(Exception e, HttpServletRequest req) {
        log.error("Storage Service Error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(ErrorCode.STORAGE_ERROR, "文件存储服务暂时不可用", getRequestId(), req.getRequestURI()));
    }

    // 处理音频转码异常
    @ExceptionHandler(com.zhupinzan.speaking.service.audio.AudioTranscodeService.AudioTranscodeException.class)
    public ResponseEntity<ApiErrorResponse> handleTranscode(Exception e, HttpServletRequest req) {
        log.error("[{}] Audio transcode failed", getRequestId(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.AUDIO_TRANSCODE_ERROR, "音频转码失败", req);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, ErrorCode code, String msg, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(code, msg, getRequestId(), req.getRequestURI()));
    }
}