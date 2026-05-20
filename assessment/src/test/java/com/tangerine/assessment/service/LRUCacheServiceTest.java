package com.tangerine.assessment.service;

import com.tangerine.assessment.entity.CacheEntry;
import com.tangerine.assessment.repository.CacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LRUCacheServiceTest {

    @Autowired
    CacheRepository repository;

    @Autowired
    LRUCacheService service;


    @BeforeEach
    void setup() {
        repository = mock(CacheRepository.class);
        service = new LRUCacheService(repository);
    }

    //  evict if capacity exceeded
    @Test
    void shouldEvictLeastRecentlyUsedWhenCapacityExceeded() {

        for (int i = 1; i <= 6; i++) {
            CacheEntry entry = new CacheEntry();
            entry.setCacheKey("k" + i);
            entry.setCacheValue("v" + i);
            entry.setLastAccessedTime(System.currentTimeMillis() + i);

            service.put("k" + i, "v" + i);
        }

        assertEquals(5, service.getAll().size());
    }

    // missing key should return null
    @Test
    void getMissingKeyShouldReturnNull() {
        assertNull(service.get("dummy-key"));
    }
}