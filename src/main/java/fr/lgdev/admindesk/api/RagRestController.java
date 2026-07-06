package fr.lgdev.admindesk.api;

import fr.lgdev.admindesk.dto.RagRequestDTO;
import fr.lgdev.admindesk.dto.RagResponseDTO;
import fr.lgdev.admindesk.service.DemandeService;
import fr.lgdev.admindesk.service.rag.RagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TP10 (D4) — Expose le RAG en REST.
 *   POST /api/v1/rag/ask           : question libre (body JSON)
 *   GET  /api/v1/rag/ask?demandeId : question générée à partir d'une demande existante
 */
@RestController
@RequestMapping("/api/v1/rag")
@Profile("prod")
@RequiredArgsConstructor
@Tag(name = "RAG", description = "Questions/réponses sourcées sur la base documentaire (RAG)")
public class RagRestController {

    private final RagService ragService;
    private final DemandeService demandeService;

    @PostMapping("/ask")
    public ResponseEntity<RagResponseDTO> ask(@Valid @RequestBody RagRequestDTO request) {
        return ResponseEntity.ok(ragService.answer(request.getQuestion()));
    }

    @GetMapping("/ask")
    public ResponseEntity<RagResponseDTO> askFromDemande(@RequestParam Long demandeId) {
        var d = demandeService.findById(demandeId);
        String q = """
                Quelle procédure s'applique pour cette demande de type %s : %s
                """.formatted(d.getType().getLibelle(), d.getDescription());
        return ResponseEntity.ok(ragService.answer(q));
    }
}
