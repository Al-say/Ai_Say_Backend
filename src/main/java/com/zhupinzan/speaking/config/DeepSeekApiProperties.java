package com.zhupinzan.speaking.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 配置统一入口。
 *
 * <p>主配置：deepseek.api.*</p>
 * <p>兼容配置（deprecated）：deepseek.base-url / deepseek.api-key / deepseek.api.key / deepseek.api.url</p>
 */
@Component
@ConfigurationProperties(prefix = "deepseek")
@Getter
@Setter
public class DeepSeekApiProperties {

    private Api api = new Api();

    /**
     * 兼容旧键：deepseek.base-url
     */
    private String baseUrl;

    /**
     * 兼容旧键：deepseek.api-key
     */
    private String apiKey;

    @Getter
    @Setter
    public static class Api {
        /**
         * 主键：deepseek.api.base-url
         */
        private String baseUrl;

        /**
         * 主键：deepseek.api.api-key
         */
        private String apiKey;

        /**
         * 兼容旧键：deepseek.api.key
         */
        private String key;

        /**
         * 主键：deepseek.api.model
         */
        private String model = "deepseek-chat";

        /**
         * 主键：deepseek.api.timeout
         */
        private Duration timeout = Duration.ofSeconds(30);

        /**
         * 兼容旧键：deepseek.api.url（通常是完整 chat/completions URL）
         */
        private String url;
    }

    public String effectiveBaseUrl() {
        if (hasText(api.getBaseUrl())) {
            return normalizeBaseUrl(api.getBaseUrl());
        }
        if (hasText(baseUrl)) {
            return normalizeBaseUrl(baseUrl);
        }
        if (hasText(api.getUrl())) {
            return normalizeBaseUrl(api.getUrl());
        }
        return "https://api.deepseek.com";
    }

    public String effectiveChatCompletionsUrl() {
        return effectiveBaseUrl() + "/v1/chat/completions";
    }

    public String effectiveApiKey() {
        if (hasText(api.getApiKey())) {
            return api.getApiKey().trim();
        }
        if (hasText(api.getKey())) {
            return api.getKey().trim();
        }
        if (hasText(apiKey)) {
            return apiKey.trim();
        }
        return "";
    }

    public String effectiveModel() {
        if (hasText(api.getModel())) {
            return api.getModel().trim();
        }
        return "deepseek-chat";
    }

    public Duration effectiveTimeout() {
        return api.getTimeout() != null ? api.getTimeout() : Duration.ofSeconds(30);
    }

    public String baseUrlSource() {
        if (hasText(api.getBaseUrl())) {
            return "deepseek.api.base-url";
        }
        if (hasText(baseUrl)) {
            return "deepseek.base-url (deprecated)";
        }
        if (hasText(api.getUrl())) {
            return "deepseek.api.url (deprecated)";
        }
        return "default";
    }

    public String apiKeySource() {
        if (hasText(api.getApiKey())) {
            return "deepseek.api.api-key";
        }
        if (hasText(api.getKey())) {
            return "deepseek.api.key (deprecated)";
        }
        if (hasText(apiKey)) {
            return "deepseek.api-key (deprecated)";
        }
        return "missing";
    }

    public boolean usesDeprecatedFallback() {
        return baseUrlSource().contains("deprecated") || apiKeySource().contains("deprecated");
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String stripTrailingSlash(String value) {
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String normalizeBaseUrl(String fullOrBaseUrl) {
        String normalized = stripTrailingSlash(fullOrBaseUrl);
        if (normalized.endsWith("/v1/chat/completions")) {
            return normalized.substring(0, normalized.length() - "/v1/chat/completions".length());
        }
        if (normalized.endsWith("/chat/completions")) {
            return normalized.substring(0, normalized.length() - "/chat/completions".length());
        }
        if (normalized.endsWith("/v1")) {
            return normalized.substring(0, normalized.length() - "/v1".length());
        }
        return normalized;
    }
}
