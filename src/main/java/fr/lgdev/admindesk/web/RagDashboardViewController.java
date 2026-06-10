package fr.lgdev.admindesk.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sert la page « RAG Quality » (/rag/dashboard).
 * La page interroge l'API REST (GET /api/v1/rag/dashboard) en JavaScript (fetch)
 * et rend les tuiles + l'histogramme avec Chart.js : ce controller ne porte aucune logique.
 */
@Controller
public class RagDashboardViewController {

    @GetMapping("/rag/dashboard")
    public String dashboard() {
        return "rag-dashboard";
    }
}
