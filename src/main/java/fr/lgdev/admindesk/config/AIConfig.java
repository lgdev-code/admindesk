package fr.lgdev.admindesk.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * TP9 — Retry programmatique (Spring Retry) pour l'appel LLM : 3 tentatives,
     * backoff exponentiel 300 ms → 600 ms → … (plafond 5 s). API impérative :
     * pas de proxy AOP, on enveloppe précisément l'appel réseau dans AIService.call().
     */
    @Bean
    public RetryTemplate llmRetryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(300, 2.0, 5000)
                .retryOn(Exception.class)
                .build();
    }
}
