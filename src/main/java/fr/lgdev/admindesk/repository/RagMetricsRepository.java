package fr.lgdev.admindesk.repository;

import fr.lgdev.admindesk.dto.SourceShare;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Requêtes d'agrégation du dashboard qualité RAG (slide 21).
 * Lit rag_audit / rag_feedback / rag_eval_run. Tout le SQL analytique est isolé ici.
 */
@Repository
@RequiredArgsConstructor
public class RagMetricsRepository {

    private final JdbcTemplate jdbcTemplate;

    /** p95 (continu) d'une colonne de latence de rag_audit sur les N derniers jours. */
    public double latencyP95(String column, int days) {
        Double v = jdbcTemplate.queryForObject("""
                SELECT percentile_cont(0.95) WITHIN GROUP (ORDER BY %s)
                FROM rag_audit
                WHERE created_at >= now() - make_interval(days => ?)
                """.formatted(column), Double.class, days);
        return v == null ? 0.0 : v;
    }

    /** % de requêtes sans aucun document remonté (retrieved_count = 0). */
    public double missRatePct(int days) {
        return ratioPct("retrieved_count = 0", days);
    }

    /** % de réponses « refus poli ». */
    public double refusalRatePct(int days) {
        return ratioPct("refused", days);
    }

    private double ratioPct(String predicate, int days) {
        Double v = jdbcTemplate.queryForObject("""
                SELECT 100.0 * avg(CASE WHEN %s THEN 1 ELSE 0 END)
                FROM rag_audit
                WHERE created_at >= now() - make_interval(days => ?)
                """.formatted(predicate), Double.class, days);
        return v == null ? 0.0 : v;
    }

    /** Ratio de votes 👍 (en %) sur l'ensemble des votes. */
    public double thumbsUpPct(int days) {
        Double v = jdbcTemplate.queryForObject("""
                SELECT 100.0 * avg(CASE WHEN vote = 'UP' THEN 1 ELSE 0 END)
                FROM rag_feedback
                WHERE created_at >= now() - make_interval(days => ?)
                """, Double.class, days);
        return v == null ? 0.0 : v;
    }

    /** Dernier recall@3 mesuré (eval set / CI, cf. TP12). */
    public double latestRecallAt3() {
        List<Double> v = jdbcTemplate.query(
                "SELECT recall_at_3 FROM rag_eval_run ORDER BY run_at DESC LIMIT 1",
                (rs, i) -> rs.getDouble(1));
        return v.isEmpty() ? 0.0 : v.get(0);
    }

    /**
     * Sources les plus citées : on éclate la colonne sources (séparée par des virgules),
     * on compte, on calcule la part, et on signale les sources peu utilisées (< 5 %).
     */
    public List<SourceShare> topSources(int days, int limit) {
        return jdbcTemplate.query("""
                WITH cited AS (
                    SELECT trim(unnest(string_to_array(sources, ','))) AS source
                    FROM rag_audit
                    WHERE created_at >= now() - make_interval(days => ?)
                      AND sources IS NOT NULL AND sources <> ''
                )
                SELECT source,
                       count(*) AS cnt,
                       round(100.0 * count(*) / sum(count(*)) OVER (), 1) AS share,
                       (round(100.0 * count(*) / sum(count(*)) OVER (), 1) < 5.0) AS low_usage
                FROM cited
                GROUP BY source
                ORDER BY cnt DESC
                LIMIT ?
                """,
                (rs, i) -> new SourceShare(
                        rs.getString("source"),
                        rs.getLong("cnt"),
                        rs.getDouble("share"),
                        rs.getBoolean("low_usage")),
                days, limit);
    }

    /**
     * Sources INDEXÉES dans vector_store mais JAMAIS ramenées par le RAG sur la fenêtre
     * (slide 22 : 0 hit en 30 jours -> revue métier : retirer ou re-chunker).
     * vector_store est dans le schéma du compte stagiaire (search_path) -> accès non qualifié.
     */
    public List<String> neverRetrievedSources(int days) {
        return jdbcTemplate.query("""
                WITH indexed AS (
                    SELECT DISTINCT metadata->>'source' AS source
                    FROM vector_store
                    WHERE metadata->>'source' IS NOT NULL
                ), used AS (
                    SELECT DISTINCT trim(unnest(string_to_array(sources, ','))) AS source
                    FROM rag_audit
                    WHERE created_at >= now() - make_interval(days => ?)
                      AND sources IS NOT NULL AND sources <> ''
                )
                SELECT i.source
                FROM indexed i
                LEFT JOIN used u ON u.source = i.source
                WHERE u.source IS NULL
                ORDER BY i.source
                """, (rs, n) -> rs.getString("source"), days);
    }
}
