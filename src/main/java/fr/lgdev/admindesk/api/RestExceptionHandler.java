package fr.lgdev.admindesk.api;

import fr.lgdev.admindesk.service.ai.AIServiceException;
import fr.lgdev.admindesk.service.ai.QuotaExceededException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(basePackages = "fr.lgdev.admindesk.api")
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail notFound(EntityNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://admindesk.lgdev.fr/errors/not-found"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validationError(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setType(URI.create("https://admindesk.lgdev.fr/errors/validation"));
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .toList());
        return problem;
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ProblemDetail quotaExceeded(QuotaExceededException ex) {
        log.warn("Quota exceeded: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setType(URI.create("https://admindesk.lgdev.fr/errors/quota-exceeded"));
        problem.setTitle("Quota IA dépassé");
        return problem;
    }

    @ExceptionHandler(AIServiceException.class)
    public ProblemDetail aiServiceError(AIServiceException ex) {
        log.error("AI service error: {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Le service d'IA est temporairement indisponible. Réessayez dans un instant.");
        problem.setType(URI.create("https://admindesk.lgdev.fr/errors/ai-service"));
        problem.setTitle("Erreur du service IA");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpectedError(Exception ex) {
        log.error("Unexpected error", ex);
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setType(URI.create("https://admindesk.lgdev.fr/errors/internal"));
        return problem;
    }
}
