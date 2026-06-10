package fr.lgdev.admindesk.service.rag;

import fr.lgdev.admindesk.dto.RagDashboardDTO;
import fr.lgdev.admindesk.repository.RagMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Assemble la vue du dashboard qualité RAG à partir des agrégations (slide 21). */
@Service
@RequiredArgsConstructor
public class RagDashboardService {

    private static final int WINDOW_DAYS = 30;
    private static final int TOP_SOURCES = 6;

    private final RagMetricsRepository metrics;

    public RagDashboardDTO load() {
        double thumbsUp = metrics.thumbsUpPct(WINDOW_DAYS);
        return new RagDashboardDTO(
                metrics.latencyP95("retrieval_ms", WINDOW_DAYS),
                metrics.latencyP95("total_ms", WINDOW_DAYS),
                metrics.missRatePct(WINDOW_DAYS),
                metrics.refusalRatePct(WINDOW_DAYS),
                thumbsUp,
                100.0 - thumbsUp,
                metrics.latestRecallAt3(),
                metrics.topSources(WINDOW_DAYS, TOP_SOURCES),
                metrics.neverRetrievedSources(WINDOW_DAYS));
    }
}
