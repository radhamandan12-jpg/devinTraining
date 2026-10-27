package com.training.loan.config;

import com.training.loan.ai.AiAssistantService;
import com.training.loan.ai.AnthropicAiService;
import com.training.loan.ai.StubAiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    @Bean
    public AiAssistantService aiAssistantService(AnthropicProperties props,
                                               RestClient.Builder restClientBuilder) {
        if (props.enabled()) {
            return new AnthropicAiService(props, restClientBuilder);
        }
        return new StubAiService();
    }
}
