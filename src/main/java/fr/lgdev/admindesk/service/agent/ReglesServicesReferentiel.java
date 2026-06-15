package fr.lgdev.admindesk.service.agent;

import fr.lgdev.admindesk.domain.TypeDemande;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Regles indicatives par service : delai d'instruction + pieces justificatives.
 *
 * <p>DONNEES DE DEMONSTRATION volontairement en memoire. Le sujet du TP est l'AGENT,
 * pas le referentiel metier. En production, ces regles viendraient d'une table ou
 * d'un referentiel officiel. Cles = {@link TypeDemande} reel (pas de type invente).</p>
 */
@Component
public class ReglesServicesReferentiel {

    public record ReglesService(int delaiJours, List<String> pieces) {
    }

    private static final ReglesService DEFAUT =
            new ReglesService(30, List.of("Piece d'identite du demandeur"));

    private final Map<TypeDemande, ReglesService> table = new EnumMap<>(TypeDemande.class);

    public ReglesServicesReferentiel() {
        table.put(TypeDemande.URBANISME, new ReglesService(90, List.of(
                "Formulaire Cerfa 13406", "Plan de situation du terrain",
                "Plan de masse des constructions", "Notice descriptive du projet")));
        table.put(TypeDemande.PERMIS, new ReglesService(60, List.of(
                "Formulaire de demande d'autorisation", "Justificatif de domicile",
                "Plan ou descriptif selon l'autorisation")));
        table.put(TypeDemande.VOIRIE, new ReglesService(15, List.of(
                "Localisation precise du desordre", "Photographie du desordre constate")));
        table.put(TypeDemande.AIDE_SOCIALE, new ReglesService(45, List.of(
                "Justificatif de ressources", "Justificatif de domicile",
                "Composition du foyer")));
        table.put(TypeDemande.INSCRIPTION_SCOLAIRE, new ReglesService(30, List.of(
                "Livret de famille", "Justificatif de domicile", "Carnet de vaccinations")));
        table.put(TypeDemande.ETAT_CIVIL, new ReglesService(20, List.of(
                "Piece d'identite du demandeur", "Justificatif du lien (le cas echeant)")));
        table.put(TypeDemande.ENVIRONNEMENT, new ReglesService(30, List.of(
                "Localisation precise", "Description de la nuisance ou du projet")));
        table.put(TypeDemande.AUTRE, DEFAUT);
    }

    public ReglesService pour(TypeDemande type) {
        return table.getOrDefault(type, DEFAUT);
    }

    public ReglesService pour(String typeRaw) {
        return pour(TypeDemandeMapper.from(typeRaw));
    }
}
