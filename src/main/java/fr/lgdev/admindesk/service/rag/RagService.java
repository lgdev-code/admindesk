package fr.lgdev.admindesk.service.rag;

import fr.lgdev.admindesk.dto.RagResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TP10 (D4) -> TP12 (D5) : flux question -> reponse sourcee, recherche HYBRIDE (RRF).
 * TP bonus D5 : chaque reponse est tracee dans rag_audit (latences, tokens, refus) et
 * un requestId est renvoye au frontend pour rattacher le feedback 👍/👎 (slide 23).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final HybridSearchService hybridSearchService;
    private final ChatClient chatClient;
    private final RagAuditService ragAuditService;

    /** Marqueur du « refus poli » : doit matcher la phrase imposee dans RAG_SYSTEM. */
    private static final String REFUSAL_MARKER = "ne figure pas dans la base documentaire";

    private static final String RAG_SYSTEM = """
            Tu es un assistant pour les agents d'une collectivite de Nouvelle-Caledonie.
            Tu reponds UNIQUEMENT a partir des extraits fournis dans le message utilisateur.
            Si la reponse ne se trouve pas dans les extraits, dis-le clairement
            (« Cette information ne figure pas dans la base documentaire. ») et ne propose rien d'autre.
            Cite TOUJOURS la ou les sources entre crochets, par exemple [CUNC-PS-PC].
            N'invente jamais d'information. Reponds en francais, de facon concise et professionnelle.
            """;

    public RagResponseDTO answer(String question) {
        String requestId = UUID.randomUUID().toString();
        long start = System.currentTimeMillis();

        // 1. Recherche HYBRIDE (RRF), top 3 — on chronometre le retrieval
        long retrievalStart = System.currentTimeMillis();
        var results = hybridSearchService.hybridSearch(question, 3);
        long retrievalMs = System.currentTimeMillis() - retrievalStart;

        if (results.isEmpty()) {
            String msg = "Aucun document pertinent.";
            ragAuditService.record(requestId, null, question, List.of(), msg,
                    0, 0, retrievalMs, System.currentTimeMillis() - start, 0, true);
            return new RagResponseDTO(msg, List.of(), requestId);
        }

        // 2. Contexte : chaque chunk prefixe par sa source
        String context = results.stream()
                .map(d -> "[%s] %s".formatted(d.getMetadata().get("source"), d.getText()))
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. Prompt RAG : on recupere la ChatResponse (texte + usage tokens), pas juste le content()
        ChatResponse cr = chatClient.prompt()
                .system(RAG_SYSTEM)
                .user("Question : " + question + "\n\nExtraits :\n" + context)
                .call()
                .chatResponse();
        String answer = cr.getResult().getOutput().getText();

        // 4. Sources distinctes (audit cote agent)
        var sources = results.stream()
                .map(d -> (String) d.getMetadata().get("source"))
                .distinct()
                .toList();

        // 5. Trace d'audit complete (slide 23) : latences, tokens, refus, nb d'extraits
        boolean refused = answer != null && answer.contains(REFUSAL_MARKER);
        var usage = cr.getMetadata().getUsage();
        ragAuditService.record(requestId, null, question, sources, answer,
                usage.getPromptTokens(), usage.getCompletionTokens(),
                retrievalMs, System.currentTimeMillis() - start, results.size(), refused);

        log.info("RAG hybride: {} chunks, sources={}, requestId={}", results.size(), sources, requestId);
        return new RagResponseDTO(answer, sources, requestId);
    }
}
