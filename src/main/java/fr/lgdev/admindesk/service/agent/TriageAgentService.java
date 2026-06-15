package fr.lgdev.admindesk.service.agent;

import fr.lgdev.admindesk.dto.PropositionTriage;
import fr.lgdev.admindesk.service.ai.AIServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Orchestre l'agent de triage.
 *
 * <p>Difference cle avec le RAG (D4/D5) : en RAG on recupere TOUJOURS, puis on repond.
 * Ici, le LLM DECIDE quels outils appeler, dans quel ordre, combien de fois, et quand
 * s'arreter. Spring AI execute cette boucle d'outils pour nous, sur {@code .call()}.</p>
 *
 * <p>On reutilise le {@link ChatClient} partage de l'application (cf. AIConfig). Les outils
 * sont passes par requete via {@code .tools(...)}, ils ne modifient pas le bean global.</p>
 */
@Service
@Slf4j
public class TriageAgentService {

    private final ChatClient chatClient;
    private final TriageTools triageTools;

    public TriageAgentService(ChatClient chatClient, TriageTools triageTools) {
        this.chatClient = chatClient;
        this.triageTools = triageTools;
    }

    private static final String SYSTEME = """
            Tu es l'agent de triage d'un guichet administratif d'une collectivite de
            Nouvelle-Caledonie. A partir de la demande d'un citoyen en langage naturel,
            tu produis une PROPOSITION de routage, structuree et justifiee.

            Methode imperative :
            1. Appelle listerTypesDemande pour connaitre les types autorises.
            2. Choisis le type le plus adapte (champ typeDemande = un nom EXACT de cette liste,
               par exemple URBANISME ou VOIRIE).
            3. Appelle reglesDuService(type) pour le delai indicatif et les pieces.
            4. Appelle rechercherReglementation pour fonder ta justification sur des sources
               officielles, et reporte ces sources dans le champ sources.

            Regles imperatives :
            - N'invente JAMAIS un type, un delai, une piece ou une source : tout vient des outils.
            - Propose une priorite parmi BASSE, NORMALE, HAUTE, URGENTE selon l'urgence percue.
            - Si la demande est hors perimetre administratif, type = AUTRE, justifie-le honnetement.
            - Tu ne crees AUCUNE demande : tu proposes seulement. Un agent humain validera.
            """;

    public PropositionTriage analyser(String demande) {
        try {
            return chatClient.prompt()
                    .system(SYSTEME)
                    .user(demande)
                    .tools(triageTools)
                    .call()
                    .entity(PropositionTriage.class);
        } catch (Exception e) {
            log.error("Echec de l'analyse de triage", e);
            throw new AIServiceException("Echec de l'analyse de triage", e);
        }
    }
}
