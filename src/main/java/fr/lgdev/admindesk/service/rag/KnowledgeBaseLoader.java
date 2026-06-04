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
            new KbDoc("proc-permis-construire.txt",    "Permis de construire (province Sud)",   "CUNC-PS-PC"),
            new KbDoc("proc-declaration-prealable.txt", "Déclaration préalable (province Sud)",  "CUNC-PS-DP"),
            new KbDoc("proc-aide-logement.txt",        "Procédure aide au logement",           "PROC-LOG-2024"),
            new KbDoc("proc-etat-civil.txt",           "État civil (compétence NC)",           "LP-2012-2"),
            new KbDoc("loi-ventes-liquidation.txt",    "Ventes en liquidation",                "LP-2014-7"),
            new KbDoc("faq-urbanisme.txt",             "FAQ urbanisme",                        "FAQ-URBA"),
            new KbDoc("tickets-urbanisme.txt",         "Tickets urbanisme résolus",            "TICKETS-URBA"));

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
