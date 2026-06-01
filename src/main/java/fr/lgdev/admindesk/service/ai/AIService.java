package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.Demande;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AdminDesk — Service IA.
 *
 * État TP3 (D1) — industrialisé :
 *  - méthode call() privée (DRY) : chrono + log + try/catch, réutilisable par les
 *    futures fonctions IA de la D2 (reformulate, detectMissingInfo, categorize) ;
 *  - observabilité : latence loggée, JAMAIS le contenu du prompt (RGPD) ;
 *  - robustesse : AIServiceException -> ProblemDetail 502 via RestExceptionHandler.
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

        return call(system, user);
    }

    /**
     * Point d'appel unique vers le LLM. Centralise chrono, log et gestion d'erreur.
     * RGPD : on ne logue JAMAIS le contenu des prompts, seulement la latence.
     */
    private String call(String system, String user) {
        long t0 = System.nanoTime();
        try {
            String content = chatClient.prompt()
                    .system(system)
                    .user(user)
                    .call()
                    .content();
            log.info("LLM call OK in {} ms", (System.nanoTime() - t0) / 1_000_000);
            return content;
        } catch (Exception e) {
            log.error("LLM call failed after {} ms",
                    (System.nanoTime() - t0) / 1_000_000, e);
            throw new AIServiceException("Échec appel IA", e);
        }
    }
}
