package fr.lgdev.admindesk.api;

import fr.lgdev.admindesk.dto.RagDashboardDTO;
import fr.lgdev.admindesk.service.rag.RagDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Données agrégées du dashboard qualité RAG (slide 21). */
@RestController
@RequestMapping("/api/v1/rag/dashboard")
@RequiredArgsConstructor
@Tag(name = "RAG Dashboard", description = "Métriques qualité du RAG en production")
public class RagDashboardRestController {

    private final RagDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<RagDashboardDTO> metrics() {
        return ResponseEntity.ok(dashboardService.load());
    }
}
