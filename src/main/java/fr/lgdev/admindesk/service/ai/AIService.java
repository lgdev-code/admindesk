package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.Demande;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AdminDesk — Service IA.
 *
 * État TP2 (D1) — prompt STRUCTURÉ : R/C/T/F/Co, séparation system/user,
 * format strict 3 lignes (Objet / Urgence / Action), refus explicite,
 * temperature 0 (cf. application.properties) pour la reproductibilité.
 *
 * Sera industrialisé au TP3 (call() privée, logs latence, AIServiceException).
 */
@Service
@RequiredArgsConstructor
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

        return chatClient.prompt()
                .system(system)
                .user(user)
                .call()
                .content();
    }
}
