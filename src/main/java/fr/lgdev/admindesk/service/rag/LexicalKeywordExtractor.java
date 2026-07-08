package fr.lgdev.admindesk.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Isole, via un appel LLM, les mots-cles distinctifs d'une question (numeros d'article,
 * sigles, notions precises) pour que la recherche LEXICALE porte sur ces termes plutot
 * que sur la question entiere (verbes, tournures interrogatives...).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LexicalKeywordExtractor {

    private final ChatClient chatClient;

    private static final String SYSTEM = """
            Tu extrais les mots-cles d'une question posee a un moteur de recherche documentaire.
            Ne garde que les termes distinctifs : numeros d'article ou de loi, sigles, codes,
            noms propres, notions juridiques ou administratives precises.
            Ignore les mots vides, verbes generiques et tournures interrogatives
            (que, quoi, comment, prevoit, dit, est-ce que...).
            Reponds UNIQUEMENT par les mots-cles separes par un espace, sans phrase, sans ponctuation.
            """;

    public String extractKeywords(String question) {
        try {
            String result = chatClient.prompt()
                    .system(SYSTEM)
                    .user(question)
                    .call()
                    .content();
            if (result == null || result.isBlank()) {
                log.warn("Extraction de mots-cles vide, repli sur la question brute");
                return question;
            }
            return result.strip();
        } catch (Exception e) {
            log.warn("Extraction de mots-cles echouee, repli sur la question brute : {}", e.getMessage());
            return question;
        }
    }
}
