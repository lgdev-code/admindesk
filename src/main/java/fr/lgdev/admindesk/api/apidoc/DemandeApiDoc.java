package fr.lgdev.admindesk.api.apidoc;

import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.dto.DemandeFormDTO;
import fr.lgdev.admindesk.dto.DemandeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "Demandes", description = "Gestion des demandes d'assistance")
public interface DemandeApiDoc {

    @Operation(
            summary = "Lister les demandes",
            description = "Retourne une page de demandes, avec filtres optionnels sur statut, type, priorité et recherche textuelle.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
            }
    )
    ResponseEntity<Page<DemandeResponseDTO>> list(
            @Parameter(description = "Filtrer par statut")       @RequestParam(required = false) StatutDemande statut,
            @Parameter(description = "Filtrer par type")          @RequestParam(required = false) TypeDemande   type,
            @Parameter(description = "Filtrer par priorité")     @RequestParam(required = false) Priorite      priorite,
            @Parameter(description = "Recherche textuelle")       @RequestParam(required = false) String        search,
            @Parameter(description = "Numéro de page (0-based)") @RequestParam(defaultValue = "0") int         page
    );

    @Operation(
            summary = "Récupérer une demande par ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Demande trouvée"),
                    @ApiResponse(responseCode = "404", description = "Demande introuvable", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> getById(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id
    );

    @Operation(
            summary = "Récupérer une demande par référence",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Demande trouvée"),
                    @ApiResponse(responseCode = "404", description = "Demande introuvable", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> getByReference(
            @Parameter(description = "Référence unique (ex: DEM-2024-00042)", required = true) @PathVariable String reference
    );

    @Operation(
            summary = "Créer une demande",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Demande créée"),
                    @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> create(
            @Valid @RequestBody DemandeFormDTO dto
    );

    @Operation(
            summary = "Modifier une demande",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Demande mise à jour"),
                    @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "404", description = "Demande introuvable", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> update(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id,
            @Valid @RequestBody DemandeFormDTO dto
    );

    @Operation(
            summary = "Supprimer une demande",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Demande supprimée"),
                    @ApiResponse(responseCode = "404", description = "Demande introuvable", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<Void> delete(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id
    );

    @Operation(
            summary = "Statistiques globales",
            description = "Retourne le total des demandes et la répartition par statut, type et priorité.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistiques calculées")
            }
    )
    ResponseEntity<Map<String, Object>> stats();

    @Operation(
            summary = "Résumer une demande via IA",
            description = "Génère un résumé structuré (Objet / Urgence / Action) et le persiste dans resumeIa.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Résumé généré"),
                    @ApiResponse(responseCode = "404", description = "Demande non trouvée", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "502", description = "Service IA indisponible", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> summarize(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id
    );

    @Operation(
            summary = "Reformuler une demande via IA",
            description = "Reformule la description en français administratif neutre (garde le sens, change le ton). "
                        + "Persiste le résultat dans reformulationIa.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reformulation générée"),
                    @ApiResponse(responseCode = "404", description = "Demande non trouvée", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "502", description = "Service IA indisponible", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> reformulate(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id
    );

    @Operation(
            summary = "Détecter les informations manquantes via IA (bonus)",
            description = "Liste les informations manquantes pour instruire la demande, sous forme de questions \"Q: …\". "
                        + "Persiste le résultat dans infosManquantesIa.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Analyse générée"),
                    @ApiResponse(responseCode = "404", description = "Demande non trouvée", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "502", description = "Service IA indisponible", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> detectMissingInfo(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id
    );

    @Operation(
            summary = "Suggérer une catégorie via IA (bonus)",
            description = "Suggère une catégorie parmi les types existants (format Catégorie / Justification). "
                        + "Persiste le résultat dans categorieSuggereeIa.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Catégorie suggérée"),
                    @ApiResponse(responseCode = "404", description = "Demande non trouvée", content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "502", description = "Service IA indisponible", content = @Content(schema = @Schema()))
            }
    )
    ResponseEntity<DemandeResponseDTO> categorize(
            @Parameter(description = "ID de la demande", required = true) @PathVariable Long id
    );
}
