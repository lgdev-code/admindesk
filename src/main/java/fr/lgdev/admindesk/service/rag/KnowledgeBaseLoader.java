package fr.lgdev.admindesk.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * TP9 (D4) — Charge la base de connaissances au démarrage et l'indexe.
 *
 * NB : ré-exécuté à CHAQUE démarrage → les vecteurs se cumulent. En formation, faire un
 * TRUNCATE de la table vector_store entre deux runs si besoin (cf. sujet).
 */
@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseLoader implements ApplicationRunner {

    private final IndexingService indexingService;

    /** fichier (dans resources/knowledge-base/), titre lisible, référence de source citée. */
    private record KbDoc(String file, String title, String source) {}

    private static final List<KbDoc> DOCS = List.of(
            // === BOTTES DE FOIN : longs textes reglementaires reels (Livre IV CCNC) ===
            new KbDoc("ccnc-prix.txt", "Code de commerce NC — Livre IV — Fixation des prix & observatoire", "CCNC-PRIX"),
            new KbDoc("ccnc-regulation-marche.txt", "Code de commerce NC — Mesures de régulation de marché (Lp. 413)", "CCNC-REGULATION"),
            new KbDoc("ccnc-anticoncurrentiel.txt", "Code de commerce NC — Pratiques anticoncurrentielles (Titre II)", "CCNC-ANTICONC"),
            new KbDoc("ccnc-concentrations.txt", "Code de commerce NC — Contrôle des concentrations (Lp. 431)", "CCNC-CONCENTRATIONS"),
            new KbDoc("ccnc-commerce-detail.txt", "Code de commerce NC — Commerce de détail (Lp. 432)", "CCNC-COMMERCE-DETAIL"),
            // === Corpus general NC (distracteurs / bruit) ===
            new KbDoc("urba-permis-construire.txt", "Permis de construire (province Sud)", "CUNC-PS-PC"),
            new KbDoc("urba-declaration-prealable.txt", "Déclaration préalable (province Sud)", "CUNC-PS-DP"),
            new KbDoc("urba-certificat-conformite.txt", "Certificat de conformité", "CUNC-PS-CC"),
            new KbDoc("urba-certificat-urbanisme.txt", "Certificat d'urbanisme (province Sud)", "CUNC-PS-CU"),
            new KbDoc("urba-permis-amenager.txt", "Permis d'aménager / lotissement", "CUNC-PS-PA"),
            new KbDoc("urba-permis-demolir.txt", "Permis de démolir", "CUNC-PS-PD"),
            new KbDoc("urba-permis-modificatif.txt", "Permis modificatif et transfert", "CUNC-PS-PM"),
            new KbDoc("urba-affichage-recours.txt", "Affichage et recours des tiers", "CUNC-AFFICHAGE"),
            new KbDoc("urba-contentieux.txt", "Contentieux de l'urbanisme", "CUNC-CONTENTIEUX"),
            new KbDoc("urba-cunc-organisation.txt", "Organisation du CUNC", "CUNC"),
            new KbDoc("urba-urbanisme-commercial.txt", "Autorisation d'urbanisme commercial", "CUNC-PS-AUC"),
            new KbDoc("urba-taxes.txt", "Taxes et participations d'urbanisme", "CUNC-TAXES"),
            new KbDoc("tickets-urbanisme.txt", "Tickets urbanisme résolus", "TICKETS-URBA"),
            new KbDoc("cafat-allocations-familiales.txt", "Allocations familiales", "CAFAT-AF"),
            new KbDoc("cafat-presentation.txt", "La CAFAT (sécurité sociale NC)", "CAFAT"),
            new KbDoc("cafat-ruamm-independants.txt", "RUAMM et indépendants", "CAFAT-RUAMM"),
            new KbDoc("cafat-retraite.txt", "Retraite (CAFAT)", "CAFAT-RETRAITE"),
            new KbDoc("cafat-complement-familial.txt", "Complément familial (CAFAT)", "CAFAT-CF"),
            new KbDoc("cafat-rentree-scolaire.txt", "Allocation de rentrée scolaire (CAFAT)", "CAFAT-ARS"),
            new KbDoc("cafat-accidents-travail.txt", "Accidents du travail (CAFAT)", "CAFAT-AT"),
            new KbDoc("cafat-cotisations.txt", "Cotisations sociales (CAFAT)", "CAFAT-COTIS"),
            new KbDoc("entreprise-ridet-patente.txt", "RIDET et patente", "RIDET"),
            new KbDoc("entreprise-rcs-kbis.txt", "RCS et extrait Kbis", "RCS-NC"),
            new KbDoc("entreprise-cfe.txt", "Centre de formalités des entreprises", "CFE-NC"),
            new KbDoc("entreprise-patente-calcul.txt", "Calcul de la patente", "PATENTE-CALCUL"),
            new KbDoc("entreprise-statuts.txt", "Choisir un statut juridique", "STATUTS-NC"),
            new KbDoc("entreprise-fiscalite.txt", "Fiscalité des entreprises (TGC, IS)", "FISCALITE-NC"),
            new KbDoc("entreprise-radiation.txt", "Modification et radiation au RIDET", "RIDET-RADIATION"),
            new KbDoc("commerce-ventes-liquidation.txt", "Ventes en liquidation", "LP-2014-7"),
            new KbDoc("commerce-soldes.txt", "Soldes", "COMMERCE-SOLDES"),
            new KbDoc("commerce-ventes-deballage.txt", "Ventes au déballage", "COMMERCE-DEBALLAGE"),
            new KbDoc("commerce-promotions.txt", "Annonces de réduction de prix", "COMMERCE-PROMO"),
            new KbDoc("commerce-concurrence.txt", "Concurrence et pratiques commerciales", "CONCURRENCE-NC"),
            new KbDoc("civil-actes.txt", "Obtenir un acte d'état civil", "ETAT-CIVIL"),
            new KbDoc("civil-naissance-declaration.txt", "Déclaration de naissance", "ETAT-CIVIL-NAISS"),
            new KbDoc("civil-mariage.txt", "Mariage civil", "ETAT-CIVIL-MARIAGE"),
            new KbDoc("civil-deces.txt", "Déclaration de décès", "ETAT-CIVIL-DECES"),
            new KbDoc("civil-pacs-concubinage.txt", "PACS et concubinage", "FAMILLE-PACS"),
            new KbDoc("civil-reconnaissance.txt", "Reconnaissance d'un enfant", "FAMILLE-RECONN"),
            new KbDoc("civil-livret-famille.txt", "Livret de famille", "ETAT-CIVIL-LIVRET"),
            new KbDoc("civil-competence.txt", "État civil — compétence NC", "LP-2012-2"),
            new KbDoc("logement-aide.txt", "Aide au logement (interne)", "PROC-LOG-2024"),
            new KbDoc("faq-urbanisme.txt", "FAQ urbanisme", "FAQ-URBA"),
            new KbDoc("tickets-divers.txt", "Tickets divers résolus", "TICKETS-DIVERS"));

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Indexation de la base de connaissances ({} documents)...", DOCS.size());
        for (KbDoc d : DOCS) {
            String content = new ClassPathResource("knowledge-base/" + d.file())
                    .getContentAsString(StandardCharsets.UTF_8);
            indexingService.indexDocument(d.title(), content, d.source());
        }
        log.info("Indexation terminée.");
    }
}
