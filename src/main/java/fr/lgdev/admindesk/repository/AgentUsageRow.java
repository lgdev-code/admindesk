package fr.lgdev.admindesk.repository;

/**
 * Spring Data projection: usages IA agrégés par agent (pour le dashboard, bonus TP7).
 * Spring construit l'objet depuis les alias de la requête JPQL — aucun DTO à mapper.
 */
public interface AgentUsageRow {
    Long getAgentId();
    long getAppels();
    long getTokens();
}
