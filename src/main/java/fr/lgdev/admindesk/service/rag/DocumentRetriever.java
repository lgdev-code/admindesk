package fr.lgdev.admindesk.service.rag;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * Abstraction d'une source de documents pour le RAG.
 * Une implémentation = une stratégie de recherche (sémantique, lexicale, ...).
 * Le service d'orchestration ne dépend que de cette interface, pas des détails d'accès.
 */
public interface DocumentRetriever {

    /** Renvoie au plus {@code topK} documents jugés pertinents pour la requête. */
    List<Document> retrieve(String query, int topK);
}
