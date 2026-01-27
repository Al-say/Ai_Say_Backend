# 日志规范与TraceId

## 背景与目的
定义日志记录规范。

## 范围与不包含
- 包含：日志级别、TraceId
- 不包含：日志存储

## 核心概念与名词表
- TraceId：请求追踪ID

## 关键流程
每个请求生成TraceId，记录日志。

## 关键接口或关键文件位置
- RequestIdFilter.java

## 常见失败模式与排障
- 日志丢失：检查配置
[返回导航](../README.md)
