package com.zhupinzan.speaking.model;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST("请求参数错误"),
    NOT_FOUND("资源不存在"),
    UNAUTHORIZED("未经授权"),
    FORBIDDEN("禁止访问"),
    VALIDATION_ERROR("数据校验失败"),
    CONFLICT("数据冲突/重复"),
    INTERNAL_ERROR("服务器内部错误"),
    AI_SERVICE_ERROR("AI 服务调用失败"); // 针对 DeepSeek 场景预留

    private final String description;

    ErrorCode(String description) {
        this.description = description;
    }
}