package fr.lgdev.admindesk.service.rag;

import fr.lgdev.admindesk.repository.RagAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Enregistre la trace d'audit de chaque réponse RAG + les votes de feedback (slide 23). */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagAuditService {

    private final RagAuditRepository ragAuditRepository;

    public void record(String requestId, Long agentId, String question, List<String> sources,
                       String answer, Integer promptTokens, Integer completionTokens,
                       long retrievalMs, long totalMs, int retrievedCount, boolean refused) {
        try {
            ragAuditRepository.insertAudit(requestId, agentId, question,
                    String.join(",", sources), answer,
                    promptTokens, completionTokens, retrievalMs, totalMs, retrievedCount, refused);
        } catch (Exception e) {
            // L'audit ne doit JAMAIS casser la réponse rendue à l'agent : on logue et on continue.
            log.warn("audit RAG non enregistré (requestId={}) : {}", requestId, e.getMessage());
        }
    }

    public void recordFeedback(String requestId, String vote) {
        ragAuditRepository.insertFeedback(requestId, vote);
    }
}
