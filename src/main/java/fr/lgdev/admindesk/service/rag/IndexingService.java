package fr.lgdev.admindesk.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * TP9 (D4) — Indexation : découpage en chunks + embedding + stockage pgvector.
 *
 * vectorStore.add(chunks) déclenche, pour chaque chunk, l'appel à OpenAI (text-embedding-3-small,
 * 1536 dimensions) puis l'INSERT dans pgvector. Aucun appel HTTP ni SQL manuel : tout est
 * encapsulé par l'abstraction Spring AI VectorStore.
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;

    public void indexDocument(String title, String content, String source) {
        // Purge des chunks déjà indexés pour cette source (évite les doublons au redémarrage)
        var b = new FilterExpressionBuilder();
        vectorStore.delete(b.eq("source", source).build());


        var doc = new Document(content, Map.of("title", title, "source", source));
        var chunks = splitter.split(List.of(doc));
        vectorStore.add(chunks);
        log.info("Indexed '{}' -> {} chunks", title, chunks.size());
    }
}
