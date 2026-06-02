package fr.lgdev.admindesk.repository;

import fr.lgdev.admindesk.domain.AiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {

    /**
     * Nombre d'usages enregistrés pour un agent sur une plage de temps
     * (typiquement le jour courant, pour le décompte du quota journalier).
     */
    long countByAgentIdAndDateUsageBetween(Long agentId, Instant start, Instant end);

    /**
     * Usages IA agrégés par agent depuis {@code start} (dashboard, bonus TP7) :
     * nombre d'appels et total de tokens, agents les plus actifs en premier.
     */
    @Query("""
           select u.agentId as agentId, count(u) as appels, coalesce(sum(u.tokens), 0) as tokens
           from AiUsage u
           where u.dateUsage >= :start
           group by u.agentId
           order by count(u) desc
           """)
    List<AgentUsageRow> usageByAgentSince(@Param("start") Instant start);
}
