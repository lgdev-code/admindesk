package fr.lgdev.admindesk.service.ai;

/**
 * Exception dédiée aux échecs d'appel au service LLM.
 * Mappée en HTTP 502 (Bad Gateway) par RestExceptionHandler — l'échec vient
 * du service en amont (Anthropic), pas d'un bug interne d'AdminDesk.
 */
public class AIServiceException extends RuntimeException {
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
