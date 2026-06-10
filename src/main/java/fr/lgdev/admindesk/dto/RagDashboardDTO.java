package fr.lgdev.admindesk.dto;

import java.util.List;

/** Vue agrégée du dashboard qualité RAG (slide 21). Tout en pourcentages / millisecondes. */
public record RagDashboardDTO(
        double retrievalP95Ms,
        double totalP95Ms,
        double missRatePct,
        double refusalRatePct,
        double thumbsUpPct,
        double thumbsDownPct,
        double recallAt3,
        List<SourceShare> topSources,
        List<String> neverRetrieved) {
}
