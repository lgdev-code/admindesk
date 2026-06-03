package fr.lgdev.admindesk.service.ai;

/**
 * Échec transient : erreur 5xx / réseau côté service LLM (Anthropic). Fait partie de la
 * whitelist de retry Resilience4j (cf. application.properties).
 */
public class AIServerException extends AIServiceException {
    public AIServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
