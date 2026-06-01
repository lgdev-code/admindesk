package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.Demande;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AdminDesk — Service IA.
 *
 * État TP4 (D2) — deuxième fonction IA : reformulate().
 *  - call() évolue : accepte désormais un agentId (préparation du QuotaService du TP6) ;
 *  - reformulate() réutilise call() : aucune duplication de la mécanique (chrono, log, try/catch).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final ChatClient chatClient;

    public String summarize(Demande demande) {

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

        return call(system, user, null);
    }

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

    /**
     * Point d'appel unique vers le LLM. Centralise chrono, log et gestion d'erreur.
     * RGPD : on ne logue JAMAIS le contenu des prompts, seulement la latence et l'agent.
     */
    private String call(String system, String user, Long agentId) {
        long t0 = System.nanoTime();
        try {
            String content = chatClient.prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .content();
            log.info("LLM call OK in {} ms — agent={}",
                    (System.nanoTime() - t0) / 1_000_000, agentId);
            return content;
        } catch (Exception e) {
            log.error("LLM call failed after {} ms — agent={}",
                    (System.nanoTime() - t0) / 1_000_000, agentId, e);
            throw new AIServiceException("Échec appel IA", e);
        }
    }
}
