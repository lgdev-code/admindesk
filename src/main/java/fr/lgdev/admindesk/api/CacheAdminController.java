package fr.lgdev.admindesk.api;

import fr.lgdev.admindesk.service.AiUsageService;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * TP7 — Admin endpoints for the AI caches (one region per AI function):
 * hit-rate stats and manual eviction. Kept outside /api/v1 on purpose: this is an
 * operational tool, not part of the public citizen-facing API.
 */
@RestController
@RequestMapping("/cache")
public class CacheAdminController {

    private final CacheManager cacheManager;
    private final AiUsageService aiUsageService;

    public CacheAdminController(CacheManager cacheManager, AiUsageService aiUsageService) {
        this.cacheManager = cacheManager;
        this.aiUsageService = aiUsageService;
    }

    /** Hit/miss/hit-rate per cache region (delegated — same data used by the dashboard). */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return aiUsageService.cacheStats();
    }

    /** Empties every AI cache region. */
    @DeleteMapping
    public ResponseEntity<Void> clearAll() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.noContent().build();
    }

    /** Empties a single region (e.g. DELETE /cache/summaries). */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> clearOne(@PathVariable String name) {
        var cache = cacheManager.getCache(name);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        cache.clear();
        return ResponseEntity.noContent().build();
    }
}
