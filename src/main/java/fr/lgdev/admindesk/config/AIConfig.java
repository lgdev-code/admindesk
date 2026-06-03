package fr.lgdev.admindesk.config;

import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AIConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    /**
     * Observabilité des retries Resilience4j. Auto-câblé dans la RetryRegistry par Spring Boot :
     * pour chaque instance Retry créée, on s'abonne aux événements onRetry afin de logger chaque
     * tentative (numéro + délai d'attente). Resilience4j ne logge rien par défaut — sans ça, les
     * retries sont invisibles. Utile en prod (monitoring) comme en test (cf. profil retrytest).
     */
    @Bean
    public RegistryEventConsumer<Retry> retryLogger() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<Retry> event) {
                event.getAddedEntry().getEventPublisher().onRetry(e ->
                        log.warn("Retry '{}' — tentative #{}, attente {}",
                                e.getName(), e.getNumberOfRetryAttempts(), e.getWaitInterval()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<Retry> event) { }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<Retry> event) { }
        };
    }
}
