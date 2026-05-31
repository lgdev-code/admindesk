package fr.lgdev.admindesk.api;

import fr.lgdev.admindesk.api.apidoc.DemandeApiDoc;
import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.dto.DemandeFormDTO;
import fr.lgdev.admindesk.dto.DemandeResponseDTO;
import fr.lgdev.admindesk.service.DemandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demandes")
@RequiredArgsConstructor
public class DemandeRestController implements DemandeApiDoc {

    private final DemandeService service;

    @Override
    @GetMapping
    public ResponseEntity<Page<DemandeResponseDTO>> list(
            @RequestParam(required = false) StatutDemande statut,
            @RequestParam(required = false) TypeDemande   type,
            @RequestParam(required = false) Priorite      priorite,
            @RequestParam(required = false) String        search,
            @RequestParam(defaultValue = "0") int         page) {

        return ResponseEntity.ok(service.search(statut, type, priorite, search, page)
                .map(DemandeResponseDTO::from));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<DemandeResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeResponseDTO.from(service.findById(id)));
    }

    @Override
    @GetMapping("/ref/{reference}")
    public ResponseEntity<DemandeResponseDTO> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(DemandeResponseDTO.from(service.findByReference(reference)));
    }

    @Override
    @PostMapping
    public ResponseEntity<DemandeResponseDTO> create(@Valid @RequestBody DemandeFormDTO dto) {
        var d = service.create(dto);
        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(d.getId())
                .toUri();
        return ResponseEntity.created(location).body(DemandeResponseDTO.from(d));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<DemandeResponseDTO> update(
            @PathVariable Long id,
                                                     @Valid @RequestBody DemandeFormDTO dto) {
        return ResponseEntity.ok(DemandeResponseDTO.from(service.update(id, dto)));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
                "total",      service.count(),
                "byStatus",   service.statsByStatus(),
                "byType",     service.statsByType(),
                "byPriority", service.statsByPriority()
        ));
    }

    @Override
    @PostMapping("/{id}/summarize")
    public ResponseEntity<DemandeResponseDTO> summarize(@PathVariable Long id) {
        return ResponseEntity.ok(DemandeResponseDTO.from(service.summarize(id)));
    }
}
