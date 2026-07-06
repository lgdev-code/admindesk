package fr.lgdev.admindesk.mcp;

import fr.lgdev.admindesk.domain.Demande;
import fr.lgdev.admindesk.domain.Priorite;
import fr.lgdev.admindesk.domain.StatutDemande;
import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.dto.DemandeResponseDTO;
import fr.lgdev.admindesk.service.DemandeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DemandeMcpTools {

    private static final int MAX_RESULTS = 50;

    private final DemandeService demandeService;

    public DemandeMcpTools(@Lazy DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @Tool(description = """
            Rechercher des demandes citoyennes selon des critères optionnels (tous filtrables ou null).
            Retourne la liste des demandes correspondantes (max 50), triées de la plus urgente à la moins urgente.""")
    public List<DemandeResponseDTO> searchDemandes(
            @ToolParam(required = false, description =
                    "Type de demande. Valeurs: URBANISME, AIDE_SOCIALE, INSCRIPTION_SCOLAIRE, ETAT_CIVIL, PERMIS, VOIRIE, ENVIRONNEMENT, AUTRE")
            String type,
            @ToolParam(required = false, description =
                    "Statut. Valeurs: EN_ATTENTE, EN_COURS, TRAITE, REJETE, EN_ATTENTE_PIECES")
            String statut,
            @ToolParam(required = false, description =
                    "Priorité. Valeurs: BASSE, NORMALE, HAUTE, URGENTE")
            String priorite,
            @ToolParam(required = false, description =
                    "Texte libre recherché dans le nom du demandeur, la description ou la référence")
            String recherche) {

        log.debug("searchDemandes: type={}, statut={}, priorite={}, recherche={}", type, statut, priorite, recherche);
        return fetchAll(parseStatut(statut), parseType(type), parsePriorite(priorite), recherche)
                .stream()
                .sorted(byUrgency())
                .limit(MAX_RESULTS)
                .map(DemandeResponseDTO::from)
                .toList();
    }

    @Tool(description = """
            Compter le nombre de demandes correspondant à des critères optionnels.
            Exemple: combien de demandes VOIRIE sont en cours (type=VOIRIE, statut=EN_COURS).""")
    public long countDemandes(
            @ToolParam(required = false, description =
                    "Type de demande. Valeurs: URBANISME, AIDE_SOCIALE, INSCRIPTION_SCOLAIRE, ETAT_CIVIL, PERMIS, VOIRIE, ENVIRONNEMENT, AUTRE")
            String type,
            @ToolParam(required = false, description =
                    "Statut. Valeurs: EN_ATTENTE, EN_COURS, TRAITE, REJETE, EN_ATTENTE_PIECES")
            String statut,
            @ToolParam(required = false, description =
                    "Priorité. Valeurs: BASSE, NORMALE, HAUTE, URGENTE")
            String priorite) {

        log.info("countDemandes: type={}, statut={}, priorite={}", type, statut, priorite);
        return demandeService.search(parseStatut(statut), parseType(type), parsePriorite(priorite), null, 0)
                .getTotalElements();
    }

    @Tool(description = """
            Retourner le détail complet de la demande la plus urgente correspondant aux critères
            (type et/ou statut optionnels). Renvoie null si aucune demande ne correspond.""")
    public DemandeResponseDTO getMostUrgentDemande(
            @ToolParam(required = false, description =
                    "Type de demande. Valeurs: URBANISME, AIDE_SOCIALE, INSCRIPTION_SCOLAIRE, ETAT_CIVIL, PERMIS, VOIRIE, ENVIRONNEMENT, AUTRE")
            String type,
            @ToolParam(required = false, description =
                    "Statut. Valeurs: EN_ATTENTE, EN_COURS, TRAITE, REJETE, EN_ATTENTE_PIECES")
            String statut) {

        log.info("getMostUrgentDemande: type={}, statut={}", type, statut);
        return fetchAll(parseStatut(statut), parseType(type), null, null)
                .stream()
                .min(byUrgency())
                .map(DemandeResponseDTO::from)
                .orElse(null);
    }

    @Tool(description = "Retourner le détail complet d'une demande à partir de sa référence (ex: DEM-2026-00123).")
    public DemandeResponseDTO getDemandeByReference(
            @ToolParam(description = "Référence unique de la demande, ex: DEM-2026-00123") String reference) {
        log.info("getDemandeByReference: reference={}", reference);
        return DemandeResponseDTO.from(demandeService.findByReference(reference));
    }

    @Tool(description = "Statistiques : nombre de demandes par type (catégorie).")
    public Map<String, Long> statsByType() {
        log.info("statsByType");
        return demandeService.statsByType();
    }

    @Tool(description = "Statistiques : nombre de demandes par statut.")
    public Map<String, Long> statsByStatus() {
        log.info("statsByStatus");
        return demandeService.statsByStatus();
    }

    @Tool(description = "Statistiques : nombre de demandes par priorité.")
    public Map<String, Long> statsByPriority() {
        log.info("statsByPriority");
        return demandeService.statsByPriority();
    }

    private List<Demande> fetchAll(StatutDemande statut, TypeDemande type, Priorite priorite, String search) {
        List<Demande> all = new ArrayList<>();
        int page = 0;
        Page<Demande> p;
        do {
            p = demandeService.search(statut, type, priorite, search, page);
            all.addAll(p.getContent());
            page++;
        } while (page < p.getTotalPages());
        return all;
    }

    private static Comparator<Demande> byUrgency() {
        return Comparator
                .comparingInt((Demande d) -> d.getPriorite().getNiveau()).reversed()
                .thenComparing(Demande::getDateCreation);
    }

    private static StatutDemande parseStatut(String v) { return parseEnum(StatutDemande.class, v); }
    private static TypeDemande   parseType(String v)   { return parseEnum(TypeDemande.class, v); }
    private static Priorite      parsePriorite(String v) { return parseEnum(Priorite.class, v); }

    private static <E extends Enum<E>> E parseEnum(Class<E> cls, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String norm = normalize(value);
        for (E e : cls.getEnumConstants()) {
            if (normalize(e.name()).equals(norm)) {
                return e;
            }
        }
        throw new IllegalArgumentException(
                "Valeur inconnue '" + value + "' pour " + cls.getSimpleName()
                        + ". Valeurs possibles: " + List.of(cls.getEnumConstants()));
    }

    private static String normalize(String s) {
        String noAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccents.toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}
