package com.tangerine.assessment.service;

import com.tangerine.assessment.dto.CacheStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/*
 * Stats Service uses Spring Cache (backed by Caffeine) to avoid
 * recomputing cache size on every request.
 *
 * IMPORTANT:
 * - Stats are invalidated whenever cache is modified (put/get/delete/clear)
 * - Ensures consistency while still improving read performance
 */
@Service
public class CacheStatsService {

    private final LRUCacheService cacheService;

    @Value("${cache.capacity:5}")
    private int capacity;

    public CacheStatsService(LRUCacheService cacheService) {
        this.cacheService = cacheService;
    }


//     Cached response using "stats" cache region
//     First call computes size, next calls return cached value
    public CacheStats getStats() {
        return new CacheStats(
                cacheService.getSize(),
                capacity
        );
    }


//     explicit eviction method used by LRUCacheService
//      ensures stats are always recalculated after mutation
    @CacheEvict(value = "stats", allEntries = true)
    public void evictStatsCache() {
        //no logic needed — annotation handles eviction
    }
}
