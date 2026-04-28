package com.zhupinzan.speaking.config;

import com.zhupinzan.speaking.model.ErrorCode;
import com.zhupinzan.speaking.model.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * API 速率限制拦截器。
 * 限制每个IP地址在一定时间窗口内的请求次数，防止API滥用。
 */
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    // 存储每个IP地址的请求信息 (请求计数和时间戳)
    private final ConcurrentMap<String, RequestInfo> ipRequestCounts = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> lastCleanup = new AtomicReference<>(Instant.EPOCH);

    // 限制参数
    private final int maxRequests;      // 最大请求次数
    private final long timeWindowMillis; // 时间窗口 (毫秒)

    public RateLimitingInterceptor(int maxRequests, long timeWindowSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowMillis = timeWindowSeconds * 1000;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        Instant now = Instant.now();
        log.info("RateLimitingInterceptor: Received request from IP: {}, Path: {}", clientIp, request.getRequestURI());
        cleanupExpiredRequests(now);

        // 获取或创建IP的请求计数器
        RequestInfo requestInfo = ipRequestCounts.computeIfAbsent(clientIp, k -> new RequestInfo(now, new AtomicInteger(0)));
        log.debug("RateLimitingInterceptor: IP: {}, Current RequestInfo: startTime={}, counter={}", clientIp, requestInfo.getStartTime(), requestInfo.getCounter().get());

        // 检查是否在当前时间窗口内
        if (requestInfo.getStartTime().plusMillis(timeWindowMillis).isBefore(now)) {
            // 时间窗口已过期，重置计数器
            requestInfo.setStartTime(now);
            requestInfo.getCounter().set(0);
            log.debug("RateLimitingInterceptor: IP: {}, Time window expired, reset counter to 0.", clientIp);
        }

        // 增加请求计数
        requestInfo.getCounter().incrementAndGet();
        log.debug("RateLimitingInterceptor: IP: {}, Incremented counter to {}", clientIp, requestInfo.getCounter().get());

        if (requestInfo.getCounter().get() > maxRequests) {
            log.warn("Rate limit exceeded for IP: {}. {} requests in {} seconds.", clientIp, maxRequests, timeWindowMillis / 1000);
            sendTooManyRequestsError(response, request.getRequestURI());
            return false; // 阻止请求继续处理
        }
        log.debug("RateLimitingInterceptor: IP: {}, Request allowed. Current count: {}", clientIp, requestInfo.getCounter().get());

        return true; // 允许请求继续处理
    }

    private void cleanupExpiredRequests(Instant now) {
        Instant previousCleanup = lastCleanup.get();
        if (!previousCleanup.plusMillis(timeWindowMillis).isBefore(now)) {
            return;
        }
        if (!lastCleanup.compareAndSet(previousCleanup, now)) {
            return;
        }
        ipRequestCounts.entrySet().removeIf(entry ->
                entry.getValue().getStartTime().plusMillis(timeWindowMillis).isBefore(now));
    }

    /**
     * 从 HttpServletRequest 中提取客户端IP地址。
     * 考虑了反向代理的情况，优先使用X-Forwarded-For。
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(".")) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    /**
     * 发送 HTTP 429 Too Many Requests 错误响应。
     */
    private void sendTooManyRequestsError(HttpServletResponse response, String path) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 使用与 GlobalExceptionHandler 相同的错误响应格式
        ApiErrorResponse errorResponse = ApiErrorResponse.of(
            ErrorCode.TOO_MANY_REQUESTS,
            "请求频率过高，请稍后重试。",
            null, // RequestId 无法在此处获取，因为拦截器在 Filter 之后
            path
        );
        response.getWriter().write(errorResponse.toJsonString());
        response.getWriter().flush();
    }

    // 内部类，存储IP的请求信息
    private static class RequestInfo {
        private volatile Instant startTime;
        private final AtomicInteger counter;

        public RequestInfo(Instant startTime, AtomicInteger counter) {
            this.startTime = startTime;
            this.counter = counter;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public void setStartTime(Instant startTime) {
            this.startTime = startTime;
        }

        public AtomicInteger getCounter() {
            return counter;
        }
    }
}
