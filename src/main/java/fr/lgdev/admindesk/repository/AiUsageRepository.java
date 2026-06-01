package fr.lgdev.admindesk.repository;

import fr.lgdev.admindesk.domain.AiUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {

    /**
     * Nombre d'usages enregistrés pour un agent sur une plage de temps
     * (typiquement le jour courant, pour le décompte du quota journalier).
     */
    long countByAgentIdAndDateUsageBetween(Long agentId, Instant start, Instant end);
}
