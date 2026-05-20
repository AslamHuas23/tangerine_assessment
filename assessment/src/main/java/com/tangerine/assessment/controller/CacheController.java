package com.tangerine.assessment.controller;

import com.tangerine.assessment.dto.CacheStats;
import com.tangerine.assessment.entity.CacheEntry;
import com.tangerine.assessment.service.CacheStatsService;
import com.tangerine.assessment.service.LRUCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cache")
public class CacheController {

    @Autowired
    LRUCacheService lruCacheService;

    @Autowired
    CacheStatsService cacheStatsService;

    public CacheController(LRUCacheService lruCacheService) {
        this.lruCacheService = lruCacheService;
    }


    @GetMapping("/{key}")
    public ResponseEntity<?> get(@PathVariable String key) {
        CacheEntry entry = lruCacheService.get(key);
        return entry == null
                ? ResponseEntity.status(404).body("Not found")
                : ResponseEntity.ok(entry);
    }

    @GetMapping("/all")
    public ResponseEntity<?> all() {
        return ResponseEntity.ok(lruCacheService.getAll());
    }

    @PostMapping
    public ResponseEntity<?> put(@RequestBody Map<String, String> req) {
        return ResponseEntity.status(201)
                .body(lruCacheService.put(req.get("key"), req.get("value")));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<?> delete(@PathVariable String key) {
        lruCacheService.delete(key);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> clear() {
        lruCacheService.clear();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/stats")
    public CacheStats stats() {
        return cacheStatsService.getStats();
    }


}
