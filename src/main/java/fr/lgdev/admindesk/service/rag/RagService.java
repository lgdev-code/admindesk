package fr.lgdev.admindesk.service.rag;

import fr.lgdev.admindesk.dto.RagResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TP10 (D4) -> TP12 (D5) : flux question -> reponse sourcee.
 * Au TP12, la recherche semantique seule est remplacee par la recherche HYBRIDE
 * (semantique + lexicale, fusion RRF) : seule la source des extraits change, le
 * reste du flux (contexte, system strict, sources) est identique.
 */
@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class RagService {

    private final HybridSearchService hybridSearchService;
    private final ChatClient chatClient;

    private static final String RAG_SYSTEM = """
            Tu es un assistant pour les agents d'une collectivité de Nouvelle-Calédonie.
            Tu réponds UNIQUEMENT à partir des extraits fournis dans le message utilisateur.
            Si la réponse ne se trouve pas dans les extraits, dis-le clairement
            (« Cette information ne figure pas dans la base documentaire. ») et ne propose rien d'autre.
            Cite TOUJOURS la ou les sources entre crochets, par exemple [CUNC-PS-PC].
            N'invente jamais d'information. Réponds en français, de façon concise et professionnelle.
            """;

    public RagResponseDTO answer(String question) {
        // 1. Recherche HYBRIDE : semantique + lexicale, fusionnees (RRF), top 3
        var results = hybridSearchService.hybridSearch(question, 3);
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

        log.info("RAG hybride: {} chunks, sources={}", results.size(), sources);
        return new RagResponseDTO(answer, sources);
    }
}
