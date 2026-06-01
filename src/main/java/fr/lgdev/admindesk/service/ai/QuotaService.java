package fr.lgdev.admindesk.service.ai;

import fr.lgdev.admindesk.domain.AiUsage;
import fr.lgdev.admindesk.repository.AiUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Quota journalier d'appels IA par agent.
 *
 * Pas de reset planifié : on compte les usages dont la date tombe dans le jour
 * courant. À minuit, "aujourd'hui" change, le compteur repart de zéro tout seul.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaService {

    private static final int DAILY_LIMIT = 20;

    private final AiUsageRepository repo;

    /** Vérifie le quota AVANT l'appel. Lève QuotaExceededException si la limite est atteinte. */
    public void check(Long agentId) {
        if (agentId == null) {
            return;   // appels sans agent identifié : hors quota
        }
        Instant start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = start.plus(1, ChronoUnit.DAYS);
        long used = repo.countByAgentIdAndDateUsageBetween(agentId, start, end);
        if (used >= DAILY_LIMIT) {
            throw new QuotaExceededException(
                    "Quota journalier atteint (" + DAILY_LIMIT + " appels) pour l'agent " + agentId);
        }
    }

    /** Enregistre un usage APRÈS un appel réussi. */
    @Transactional
    public void recordUsage(Long agentId, int tokens) {
        if (agentId == null) {
            return;
        }
        repo.save(new AiUsage(agentId, Instant.now(), tokens));
    }
}
