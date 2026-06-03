package fr.lgdev.admindesk.service.ai;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * TP8 — Appel LLM « durci » par Resilience4j.
 *
 * Pourquoi un bean dédié plutôt que d'annoter summarize() ? Depuis le TP7, summarize() &amp; co
 * sont @Cacheable : empiler dessus @TimeLimiter (qui impose un retour CompletableFuture, donc
 * de l'async) se battrait avec le cache. On isole donc l'appel réseau ici. Le cache reste au
 * niveau des fonctions publiques (un hit court-circuite tout, y compris cette résilience).
 *
 * Ordre des garde-fous : CircuitBreaker &gt; Retry &gt; TimeLimiter (extérieur vers intérieur).
 * @TimeLimiter exige un CompletableFuture exécuté de façon asynchrone.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LlmClient {

    private final ChatClient chatClient;

    @CircuitBreaker(name = "anthropic-api", fallbackMethod = "fallback")
    @Retry(name = "anthropic-api")
    @TimeLimiter(name = "anthropic-api")
    public CompletableFuture<String> complete(String system, String safeUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chatClient.prompt()
                        .system(system)
                        .user(safeUser)
                        .call()
                        .content();
            } catch (Exception e) {
                // Classify so the retry whitelist (timeout / server errors) can match.
                throw classify(e);
            }
        });
    }

    /**
     * Invoked when the circuit is OPEN or every retry failed. Must be fast, sync-ish and not
     * call the LLM again. We surface a clean AIServiceException → 502 ProblemDetail (RGPD-safe).
     */
    private CompletableFuture<String> fallback(String system, String safeUser, Throwable t) {
        log.warn("AI fallback triggered (circuit open or retries exhausted): {}", t.toString());
        return CompletableFuture.failedFuture(
                new AIServiceException("Service IA temporairement indisponible", t));
    }

    private RuntimeException classify(Exception e) {
        if (e instanceof AIServiceException ase) {
            return ase;
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("timeout") || msg.contains("timed out") || e instanceof java.util.concurrent.TimeoutException) {
            return new AITimeoutException("Timeout appel IA", e);
        }
        // Auth / erreurs client 4xx (401/403) : NON-transitoires. Réessayer une clé invalide
        // ou des droits manquants est inutile → on renvoie l'AIServiceException de base
        // (hors whitelist de retry) pour échouer vite vers le fallback.
        if (msg.contains("401") || msg.contains("403")
                || msg.contains("authentication") || msg.contains("invalid x-api-key")
                || msg.contains("permission")) {
            return new AIServiceException("Clé/credentials IA invalides ou refusés", e);
        }
        // Network / 5xx upstream → transient, eligible for retry.
        return new AIServerException("Erreur serveur IA", e);
    }
}
