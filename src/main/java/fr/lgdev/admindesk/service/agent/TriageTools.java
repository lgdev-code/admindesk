package fr.lgdev.admindesk.service.agent;

import fr.lgdev.admindesk.domain.TypeDemande;
import fr.lgdev.admindesk.service.rag.HybridSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Les outils que l'agent de triage a le DROIT d'appeler. TOUS EN LECTURE SEULE.
 *
 * <p>Choix de conception central : il n'y a PAS d'outil « creerDemande » ici.
 * Un outil annote {@code @Tool} est une fonction que le LLM peut declencher
 * lui-meme, sans demander. C'est acceptable pour LIRE. Ca ne l'est jamais pour
 * un acte qui modifie le monde (creer une demande = ecriture en base + engagement
 * de l'administration). L'agent PROPOSE ; un humain VALIDE ; du code deterministe
 * cree la demande (voir TriageViewController#valider).</p>
 */
@Component
@RequiredArgsConstructor
public class TriageTools {

    private final HybridSearchService hybridSearchService;
    private final ReglesServicesReferentiel regles;
    private final TriageTrace trace;

    @Tool(description = """
            Recherche dans la base documentaire reglementaire de la collectivite les extraits
            officiels pertinents pour une demande citoyenne. A appeler pour FONDER le routage
            sur des sources reelles. Retourne des extraits, chacun prefixe par sa source entre
            crochets, par exemple [CUNC-PS-PC]. Si rien n'est trouve, le signale explicitement.""")
    public String rechercherReglementation(
            @ToolParam(description = "La demande du citoyen, reformulee en une question claire")
            String question) {
        trace.log("rechercherReglementation(\"" + question + "\")");
        List<Document> results = hybridSearchService.hybridSearch(question, 3);
        if (results.isEmpty()) {
            return "Aucun extrait pertinent trouve dans la base documentaire.";
        }
        return results.stream()
                .map(d -> "[%s] %s".formatted(d.getMetadata().get("source"), d.getText()))
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    @Tool(description = """
            Liste les types de demande autorises dans AdminDesk. Le routage DOIT utiliser
            l'un de ces types (champ typeDemande de la proposition), jamais un type invente.""")
    public List<String> listerTypesDemande() {
        trace.log("listerTypesDemande()");
        return Arrays.stream(TypeDemande.values())
                .map(t -> "%s (%s)".formatted(t.name(), t.getLibelle()))
                .toList();
    }

    @Tool(description = """
            Renvoie les regles indicatives d'un type de demande : delai d'instruction en jours
            et liste des pieces justificatives a fournir. A appeler une fois le type identifie.""")
    public String reglesDuService(
            @ToolParam(description = "Le type de demande identifie, ex. URBANISME ou VOIRIE")
            String typeDemande) {
        trace.log("reglesDuService(\"" + typeDemande + "\")");
        var r = regles.pour(typeDemande);
        return "Delai indicatif : %d jours. Pieces requises : %s."
                .formatted(r.delaiJours(), String.join(", ", r.pieces()));
    }
}
