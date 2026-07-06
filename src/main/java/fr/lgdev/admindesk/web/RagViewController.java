package fr.lgdev.admindesk.web;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * TP10 (bonus, D4) — sert la page « Assistant RAG » (/rag).
 *
 * La page elle-même interroge l'API REST (POST /api/v1/rag/ask) en JavaScript (fetch) :
 * ce controller ne fait que servir le template, il ne porte aucune logique RAG.
 */
@Controller
@Profile("prod")
public class RagViewController {

    @GetMapping("/rag")
    public String ragPage() {
        return "rag";
    }
}
