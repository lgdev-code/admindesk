package fr.lgdev.admindesk.service.rag;

import fr.lgdev.admindesk.dto.RagResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TP10 (D4) — RAG opérationnel : flux question → réponse sourcée.
 *
 *   1. recherche sémantique (similaritySearch topK=3) dans pgvector ;
 *   2. garde-fou si aucun extrait pertinent ;
 *   3. construction du contexte (chaque chunk préfixé par sa source) ;
 *   4. appel chat avec un system STRICT (répondre seulement depuis les extraits, citer, refus poli) ;
 *   5. extraction des sources distinctes pour l'audit.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    /**
     * System strict = LE levier anti-hallucination : on interdit de répondre hors extraits,
     * on impose la citation des sources, on impose le refus poli si l'info est absente.
     */
    private static final String RAG_SYSTEM = """
            Tu es un assistant pour les agents d'une collectivité de Nouvelle-Calédonie.
            Tu réponds UNIQUEMENT à partir des extraits fournis dans le message utilisateur.
            Si la réponse ne se trouve pas dans les extraits, dis-le clairement
            (« Cette information ne figure pas dans la base documentaire. ») et ne propose rien d'autre.
            Cite TOUJOURS la ou les sources entre crochets, par exemple [CUNC-PS-PC].
            N'invente jamais d'information. Réponds en français, de façon concise et professionnelle.
            """;

    public RagResponseDTO answer(String question) {
        // 1. Recherche sémantique : les 3 chunks les plus proches de la question
        var results = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build());
        if (results.isEmpty()) {
            return new RagResponseDTO("Aucun document pertinent.", List.of());
        }

        // 2. Contexte : chaque chunk préfixé par sa source
        String context = results.stream()
                .map(d -> "[%s] %s".formatted(d.getMetadata().get("source"), d.getText()))
                .collect(Collectors.joining("\n\n---\n\n"));

        // 3. Prompt RAG : system strict + user (question + extraits)
        String answer = chatClient.prompt()
                .system(RAG_SYSTEM)
                .user("Question : " + question + "\n\nExtraits :\n" + context)
                .call()
                .content();

        // 4. Sources distinctes pour l'audit
        var sources = results.stream()
                .map(d -> (String) d.getMetadata().get("source"))
                .distinct()
                .toList();

        log.info("RAG: {} chunks, sources={}", results.size(), sources);
        return new RagResponseDTO(answer, sources);
    }
}
