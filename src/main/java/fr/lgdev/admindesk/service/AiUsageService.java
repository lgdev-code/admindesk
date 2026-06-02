package fr.lgdev.admindesk.service;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import fr.lgdev.admindesk.repository.AgentUsageRow;
import fr.lgdev.admindesk.repository.AiUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Monitoring IA léger (bonus TP7) : usages agrégés par agent et statistiques du cache.
 * Volontairement simple : pas de coût en € (cela demanderait d'enrichir AiUsage avec
 * tokensIn/out + un PricingService — hors périmètre de ce TP).
 */
@Service
@RequiredArgsConstructor
public class AiUsageService {

    private final AiUsageRepository aiUsageRepository;
    private final CacheManager cacheManager;

    /** Usages IA par agent depuis le début du jour courant. */
    public List<AgentUsageRow> usageByAgentToday() {
        Instant start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        return aiUsageRepository.usageByAgentSince(start);
    }

    /** Hit/miss/hit-rate par région de cache (nécessite recordStats dans la spec). */
    public Map<String, Object> cacheStats() {
        Map<String, Object> all = new LinkedHashMap<>();
        for (String name : cacheManager.getCacheNames()) {
            if (cacheManager.getCache(name) instanceof CaffeineCache caffeine) {
                CacheStats s = caffeine.getNativeCache().stats();
                all.put(name, Map.of(
                        "hitCount", s.hitCount(),
                        "missCount", s.missCount(),
                        "hitRate", s.hitRate(),
                        "estimatedSize", caffeine.getNativeCache().estimatedSize()));
            }
        }
        return all;
    }
}
