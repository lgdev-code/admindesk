package fr.lgdev.admindesk.dto;

import java.util.List;

/**
 * TP10 (D4) — Réponse RAG : le texte généré + les sources distinctes ramenées,
 * affichées à l'agent pour l'audit (vérification que la matière existe en base).
 */
public record RagResponseDTO(String answer, List<String> sources) {
}
