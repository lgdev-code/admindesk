package fr.lgdev.admindesk.service.ai;

/**
 * Échec transient : l'appel au LLM a dépassé le délai. Fait partie de la whitelist
 * de retry Resilience4j (cf. application.properties).
 */
public class AITimeoutException extends AIServiceException {
    public AITimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
