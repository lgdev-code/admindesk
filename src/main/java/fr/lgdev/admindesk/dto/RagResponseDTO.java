package fr.lgdev.admindesk.dto;

import java.util.List;

/**
 * Réponse RAG : le texte généré + les sources distinctes ramenées + le requestId.
 * Le requestId (TP bonus D5) identifie la trace d'audit ; le frontend le renvoie avec le 👍/👎.
 */
public record RagResponseDTO(String answer, List<String> sources, String requestId) {
}
