package com.gservices.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache applicatif (in-memory). Les référentiels quasi statiques (continents,
 * pays, unités de mesure, jours/mois…) seront mis en cache à partir du Lot 2.
 * Un provider distribué (Caffeine/Redis) pourra remplacer cette implémentation
 * sans impacter la couche service.
 */
@Configuration
public class CacheConfig {

    public static final String REFERENTIEL = "referentiel";
    public static final String MENU_ADMIN  = "menuAdmin";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(REFERENTIEL, MENU_ADMIN);
    }
}
