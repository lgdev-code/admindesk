package fr.lgdev.admindesk.service.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestration de la recherche HYBRIDE : combine plusieurs {@link DocumentRetriever}
 * (sémantique + lexical) et fusionne leurs classements par Reciprocal Rank Fusion (RRF).
 * Aucune dépendance à SQL ni à Spring AI ici : logique pure, donc directement testable.
 */
@Service
@Slf4j
public class HybridSearchService {

    private static final int RRF_K = 60;        // constante RRF standard
    private static final int CANDIDATES = 10;   // top-N de chaque source avant fusion

    private final DocumentRetriever semanticRetriever;
    private final DocumentRetriever lexicalRetriever;

    public HybridSearchService(
            @Qualifier("semanticRetriever") DocumentRetriever semanticRetriever,
            @Qualifier("lexicalDocumentRepository") DocumentRetriever lexicalRetriever) {
        this.semanticRetriever = semanticRetriever;
        this.lexicalRetriever = lexicalRetriever;
    }

    public List<Document> hybridSearch(String question, int topK) {
        List<Document> semantic = semanticRetriever.retrieve(question, CANDIDATES);
        List<Document> lexical = lexicalRetriever.retrieve(question, CANDIDATES);
        log.info("Hybride: {} sémantiques + {} lexicaux", semantic.size(), lexical.size());
        return reciprocalRankFusion(List.of(semantic, lexical), topK);
    }

    /**
     * RRF : pour chaque document, score = somme sur les listes de 1 / (k + rang).
     * On ne combine que des RANGS -> aucune normalisation de scores hétérogènes.
     */
    private List<Document> reciprocalRankFusion(List<List<Document>> rankings, int topK) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Document> byId = new LinkedHashMap<>();
        for (List<Document> ranking : rankings) {
            int rank = 1;
            for (Document doc : ranking) {
                String id = doc.getId();
                scores.merge(id, 1.0 / (RRF_K + rank), Double::sum);
                byId.putIfAbsent(id, doc);
                rank++;
            }
        }
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> byId.get(entry.getKey()))
                .toList();
    }
}
