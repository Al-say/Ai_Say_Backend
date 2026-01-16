package com.zhupinzan.speaking.model.dto;

import com.zhupinzan.speaking.model.ErrorCode;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        boolean success,
        ErrorCode code,
        String message,
        String requestId,
        String path,
        OffsetDateTime timestamp
) {
    public static ApiErrorResponse of(ErrorCode code, String message, String requestId, String path) {
        return new ApiErrorResponse(false, code, message, requestId, path, OffsetDateTime.now());
    }
}