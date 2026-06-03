package fr.lgdev.admindesk.service.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Garde-fou de non-régression sur la résilience de l'appel LLM (TP8).
 *
 * On charge un vrai contexte Spring (les garde-fous Resilience4j sont des aspects AOP : ils
 * n'existent que sur un bean proxifié, pas sur un {@code new LlmClient()}). On mocke le maillon
 * bas {@link ChatModel} ; le vrai {@code ChatClient} (cf. AIConfig) l'enveloppe. Compter les
 * invocations de {@code chatModel.call()} mesure donc directement le nombre de tentatives.
 *
 * Vérifie les deux comportements voulus :
 *  - erreur TRANSITOIRE  -> 3 tentatives (1 + 2 retries) puis fallback ;
 *  - erreur d'AUTH (401) -> 1 seule tentative (fail-fast), AUCUN retry.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        // Clé bidon : évite que l'autoconfig Anthropic échoue au démarrage. Aucun appel réseau
        // n'est émis puisque ChatModel est mocké.
        "spring.ai.anthropic.api-key=test-dummy-key",
        // Backoff réduit : test rapide et déterministe (sinon 1s puis 2s d'attente réelle).
        "resilience4j.retry.instances.anthropic-api.wait-duration=10ms",
        "resilience4j.retry.instances.anthropic-api.exponential-backoff-multiplier=1"
})
class LlmClientRetryTest {

    @MockitoBean
    private ChatModel chatModel;

    @Autowired
    private LlmClient llmClient;

    @Test
    void retente_une_erreur_transitoire_puis_bascule_en_fallback() {
        // Erreur quelconque (ni timeout ni 401) -> classify() -> AIServerException (retryable).
        when(chatModel.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("Simulated upstream failure"));

        assertThatThrownBy(() -> llmClient.complete("system", "user").join())
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(AIServiceException.class);   // fallback déclenché

        // max-attempts=3 = 1 appel initial + 2 retries.
        verify(chatModel, times(3)).call(any(Prompt.class));
    }

    @Test
    void ne_retente_pas_une_erreur_d_authentification() {
        // 401 -> classify() -> AIServiceException de base, HORS whitelist de retry -> fail-fast.
        when(chatModel.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("HTTP 401 - invalid x-api-key"));

        assertThatThrownBy(() -> llmClient.complete("system", "user").join())
                .isInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(AIServiceException.class);

        // Une clé invalide ne doit JAMAIS être réessayée.
        verify(chatModel, times(1)).call(any(Prompt.class));
    }
}
