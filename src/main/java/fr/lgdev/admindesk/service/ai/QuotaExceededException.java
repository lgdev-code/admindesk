package fr.lgdev.admindesk.service.ai;

/**
 * Levée quand un agent a atteint son quota journalier d'appels IA.
 * Mappée en HTTP 429 (Too Many Requests) par RestExceptionHandler.
 */
public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) {
        super(message);
    }
}
