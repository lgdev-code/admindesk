package fr.lgdev.admindesk.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Écritures de l'observabilité RAG : trace d'audit (rag_audit) et votes (rag_feedback). */
@Repository
@RequiredArgsConstructor
public class RagAuditRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertAudit(String requestId, Long agentId, String question, String sources,
                            String answer, Integer promptTokens, Integer completionTokens,
                            long retrievalMs, long totalMs, int retrievedCount, boolean refused) {
        jdbcTemplate.update("""
                INSERT INTO rag_audit (request_id, agent_id, question, sources, answer,
                                       prompt_tokens, completion_tokens, retrieval_ms, total_ms,
                                       retrieved_count, refused)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                requestId, agentId, question, sources, answer,
                promptTokens, completionTokens, retrievalMs, totalMs, retrievedCount, refused);
    }

    public void insertFeedback(String requestId, String vote) {
        jdbcTemplate.update(
                "INSERT INTO rag_feedback (request_id, vote) VALUES (?, ?)",
                requestId, vote);
    }
}
