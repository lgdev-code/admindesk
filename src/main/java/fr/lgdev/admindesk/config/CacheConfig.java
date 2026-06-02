package fr.lgdev.admindesk.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's declarative caching. The CacheManager itself is auto-configured
 * by Spring Boot from spring.cache.type=caffeine and spring.cache.caffeine.spec.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
