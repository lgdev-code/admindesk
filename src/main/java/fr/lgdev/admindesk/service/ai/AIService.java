package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.Demande;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AdminDesk — Service IA.
 *
 * État TP1 (D1) — prompt NAÏF, sans system, sans format imposé, sans contraintes.
 * Volontairement minimaliste. Sera amélioré au TP2 (R/C/T/F/Co)
 * puis industrialisé au TP3 (call() privée, logs, AIServiceException).
 */
@Service
@RequiredArgsConstructor
public class AIService {

    private final ChatClient chatClient;

    public String summarize(Demande demande) {
        return chatClient.prompt()
                .user("Résume cette demande : " + demande.getDescription())
                .call()
                .content();
    }
}
