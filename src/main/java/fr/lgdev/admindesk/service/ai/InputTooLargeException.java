package fr.lgdev.admindesk.service.ai;

/**
 * Saisie trop longue pour être envoyée au LLM. Mappée en HTTP 400 (Bad Request) :
 * c'est une erreur de la requête cliente, pas une panne du service.
 */
public class InputTooLargeException extends RuntimeException {
    public InputTooLargeException(String message) {
        super(message);
    }
}
