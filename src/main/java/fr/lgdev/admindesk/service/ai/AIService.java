package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.Demande;
import fr.lgdev.admindesk.domain.TypeDemande;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AdminDesk — Service IA.
 *
 * État TP6 (D2) — forme finale de call() : quota -> masquage RGPD -> appel LLM -> comptage.
 *  - QuotaService.check() avant l'appel (peut lever 429) ;
 *  - InputSanitizer.sanitize() masque le user ;
 *  - QuotaService.recordUsage() APRÈS succès uniquement (jamais sur échec).
 *
 * État TP7 (D3) — les 4 fonctions IA sont @Cacheable (une région de cache par fonction :
 *  summaries / reformulations / infos-manquantes / categories), clé = hash du contenu.
 *  Un cache hit court-circuite la méthode : zéro appel LLM, quota intact.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatClient chatClient;
    private final InputSanitizer sanitizer;
    private final QuotaService quotas;

    @Cacheable(value = "summaries",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String summarize(Demande demande, Long agentId) {

        String system = """
                Tu es un agent administratif expert.
                Tu traites des demandes de citoyens calédoniens adressées à une collectivité.

                Tu réponds TOUJOURS exactement dans ce format, sur 3 lignes,
                sans introduction ni conclusion :
                Objet : <résumé de la demande en une phrase>
                Urgence : <Oui ou Non, suivi d'une courte justification>
                Action suggérée : <la prochaine étape concrète pour l'agent>

                Contraintes :
                - Réponds uniquement en français.
                - N'invente AUCUNE information absente de la demande.
                - Reste neutre et professionnel.
                - Si la demande ne relève pas d'un service administratif
                  ou si elle est inexploitable, écris exactement :
                  "Objet : demande hors périmètre ou inexploitable"
                  et laisse les deux autres lignes vides.
                """;

        String user = """
                Analyse et résume la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    @Cacheable(value = "reformulations",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String reformulate(Demande demande, Long agentId) {

        String system = """
                Tu es un agent administratif.
                Reformule en français administratif neutre.
                Garde le sens, change le ton.
                N'invente rien, n'ajoute rien.
                Si la demande est déjà formelle, retourne-la quasi à l'identique.
                """;

        String user = """
                Reformule la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    @Cacheable(value = "infos-manquantes",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String detectMissingInfo(Demande demande, Long agentId) {

        String system = """
                Tu es un agent administratif.
                Liste les informations manquantes ou imprécises nécessaires pour instruire la demande.
                Réponds UNIQUEMENT par des questions courtes, une par ligne, chacune préfixée par "Q: ".
                N'invente rien, ne reformule pas la demande.
                Si la demande est complète, réponds exactement : "Aucune information manquante."
                """;

        String user = """
                Identifie les informations manquantes dans la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    @Cacheable(value = "categories",
               key = "T(fr.lgdev.admindesk.util.Hash).sha256(#demande.description)")
    public String categorize(Demande demande, Long agentId) {

        // Build the allowed list from the enum so the prompt stays in sync with TypeDemande.
        String categories = Arrays.stream(TypeDemande.values())
                .map(TypeDemande::getLibelle)
                .collect(Collectors.joining(", "));

        String system = """
                Tu es un agent administratif chargé du tri des demandes citoyennes.
                Choisis la catégorie la plus adaptée parmi la liste autorisée, et elle seule.
                N'invente aucune catégorie hors de cette liste.

                Réponds TOUJOURS exactement dans ce format, sans phrase autour :
                Catégorie : <un libellé exact de la liste autorisée>
                Justification : <une phrase courte>

                Liste autorisée : %s
                """.formatted(categories);

        String user = """
                Catégorise la demande suivante :

                %s
                """.formatted(demande.getDescription());

        return call(system, user, agentId);
    }

    /**
     * Point d'appel unique vers le LLM. Centralise chrono, log et gestion d'erreur.
     * RGPD : on ne logue JAMAIS le contenu des prompts, seulement la latence et l'agent.
     */
    private String call(String system, String user, Long agentId) {
        quotas.check(agentId);                          // garde-fou quota (peut lever 429)
        long t0 = System.nanoTime();
        String safeUser = sanitizer.sanitize(user);   // masquage RGPD avant envoi
        try {
            String content = chatClient.prompt()
                    .system(system)
                    .user(safeUser)
                    .call()
                    .content();
            log.info("LLM call OK in {} ms — agent={}",
                    (System.nanoTime() - t0) / 1_000_000, agentId);
            quotas.recordUsage(agentId, estimateTokens(content));  // APRÈS succès uniquement
            return content;
        } catch (Exception e) {
            log.error("LLM call failed after {} ms — agent={}",
                    (System.nanoTime() - t0) / 1_000_000, agentId, e);
            throw new AIServiceException("Échec appel IA", e);
        }
    }

    /** Estimation grossière suffisante pour le décompte du quota (~1 token pour 4 caractères). */
    private int estimateTokens(String text) {
        return text == null ? 0 : text.length() / 4;
    }
}
