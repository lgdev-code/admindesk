package fr.lgdev.admindesk.service.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Recherche sémantique : simple adaptateur autour du VectorStore (proximité de sens).
 * Isolé derrière {@link DocumentRetriever} pour que l'orchestration ne connaisse pas Spring AI.
 */
@Component
@RequiredArgsConstructor
public class SemanticRetriever implements DocumentRetriever {

    private final VectorStore vectorStore;

    @Override
    public List<Document> retrieve(String query, int topK) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build());
    }
}
