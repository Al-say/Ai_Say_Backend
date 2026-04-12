package com.zhupinzan.speaking.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class DeepSeekApiPropertiesTest {

    @Test
    void shouldUseNewKeysWhenOnlyNewKeysProvided() {
        DeepSeekApiProperties props = new DeepSeekApiProperties();
        props.getApi().setBaseUrl("https://new.example.com");
        props.getApi().setApiKey("new-key");
        props.getApi().setModel("deepseek-v3");
        props.getApi().setTimeout(Duration.ofSeconds(45));

        assertEquals("https://new.example.com", props.effectiveBaseUrl());
        assertEquals("https://new.example.com/v1/chat/completions", props.effectiveChatCompletionsUrl());
        assertEquals("new-key", props.effectiveApiKey());
        assertEquals("deepseek-v3", props.effectiveModel());
        assertEquals(Duration.ofSeconds(45), props.effectiveTimeout());
        assertEquals("deepseek.api.base-url", props.baseUrlSource());
        assertEquals("deepseek.api.api-key", props.apiKeySource());
        assertFalse(props.usesDeprecatedFallback());
    }

    @Test
    void shouldFallbackToDeprecatedKeysWhenNewKeysMissing() {
        DeepSeekApiProperties props = new DeepSeekApiProperties();
        props.setBaseUrl("https://legacy.example.com/v1");
        props.setApiKey("legacy-root-key");
        props.getApi().setUrl("https://legacy-from-url.example.com/v1/chat/completions");
        props.getApi().setKey("legacy-api-key");

        assertEquals("https://legacy.example.com", props.effectiveBaseUrl());
        assertEquals("legacy-api-key", props.effectiveApiKey());
        assertEquals("deepseek.base-url (deprecated)", props.baseUrlSource());
        assertEquals("deepseek.api.key (deprecated)", props.apiKeySource());
        assertTrue(props.usesDeprecatedFallback());
    }

    @Test
    void shouldPreferNewKeysWhenBothNewAndDeprecatedExist() {
        DeepSeekApiProperties props = new DeepSeekApiProperties();
        props.getApi().setBaseUrl("https://new.example.com/v1");
        props.getApi().setApiKey("new-key");
        props.getApi().setModel("new-model");

        props.setBaseUrl("https://legacy-root.example.com");
        props.setApiKey("legacy-root-key");
        props.getApi().setUrl("https://legacy-url.example.com/v1/chat/completions");
        props.getApi().setKey("legacy-api-key");

        assertEquals("https://new.example.com", props.effectiveBaseUrl());
        assertEquals("new-key", props.effectiveApiKey());
        assertEquals("new-model", props.effectiveModel());
        assertEquals("deepseek.api.base-url", props.baseUrlSource());
        assertEquals("deepseek.api.api-key", props.apiKeySource());
        assertFalse(props.usesDeprecatedFallback());
    }
}
