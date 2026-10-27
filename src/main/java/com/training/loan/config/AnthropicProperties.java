package com.training.loan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        String baseUrl,
        String model,
        int maxTokens,
        int timeoutSeconds
) {
    public AnthropicProperties {
        if (baseUrl == null || baseUrl.isBlank()) baseUrl = "https://api.anthropic.com";
        if (model == null || model.isBlank()) model = "claude-sonnet-4-5-20250929";
        if (maxTokens <= 0) maxTokens = 2048;
        if (timeoutSeconds <= 0) timeoutSeconds = 30;
    }

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }
}
