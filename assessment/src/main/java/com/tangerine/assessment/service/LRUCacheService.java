package com.tangerine.assessment.service;

import com.tangerine.assessment.entity.CacheEntry;
import com.tangerine.assessment.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LRUCacheService {

    @Autowired
    CacheRepository repository;

    private final Map<String, CacheEntry> cache = new HashMap<>();


//     LOCKING STRATEGY: ReentrantLock (single global lock)
//     Why:
//     LRU cache modifies shared state on BOTH get() and put()
//     Even "read" operations mutate access order
//     Therefore ReadWriteLock provides no benefit
//
//      Why ReadWriteLock is not used
//          get() is not read-only in LRU (it updates lastAccessedTime + ordering)
//          Using read locks would cause race conditions in eviction logic
//          Can lead to incorrect LRU ordering under concurrency
//
//     ReentrantLock ensures:
//     Atomic updates to cache + DB + eviction logic
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${cache.capacity:5}")
    private int capacity;

    public LRUCacheService(CacheRepository repository) {
        this.repository = repository;
    }

//    MySQL is used for persistence so cache survives restart.
//    Trade-off: slower than Redis but durable and queryable.
     // Load cache on startup
    @PostConstruct
    public void loadCache() {
        List<CacheEntry> entries =
                repository.findAllByOrderByLastAccessedTimeAsc();

        for (CacheEntry e : entries) {
            cache.put(e.getCacheKey(), e);
        }
    }


    // to get cache by key
    public CacheEntry get(String key) {
        lock.lock();
        try {
            CacheEntry entry = cache.get(key);
            if (entry == null) return null;

            entry.setLastAccessedTime(System.currentTimeMillis());
            repository.save(entry);

            cache.put(key, entry);
            return entry;
        } finally {
            lock.unlock();
        }
    }


    // to save cache by key
    public CacheEntry put(String key, String value) {
        lock.lock();
        try {
            if (cache.containsKey(key)) {
                CacheEntry existing = cache.get(key);
                existing.setCacheValue(value);
                existing.setLastAccessedTime(System.currentTimeMillis());
                cache.put(key, existing);
                repository.save(existing);
                return existing;
            }

            CacheEntry entry = new CacheEntry();
            entry.setCacheKey(key);
            entry.setCacheValue(value);
            entry.setLastAccessedTime(System.currentTimeMillis());

            cache.put(key, entry);
            repository.save(entry);

            if (cache.size() > capacity) {
                evictLRU();
            }

            return entry;
        } finally {
            lock.unlock();
        }
    }


    // delet a cacheEntry by key
    public void delete(String key) {
        lock.lock();
        try {
            cache.remove(key);
            repository.deleteById(key);
        } finally {
            lock.unlock();
        }
    }

    // to clear all cache entries
    public void clear() {
        lock.lock();
        try {
            cache.clear();
            repository.deleteAll();
        } finally {
            lock.unlock();
        }
    }


    // to get all cache entries
    public List<CacheEntry> getAll() {
        lock.lock();
        try {
            return cache.values()
                    .stream()
                    .sorted((a, b) ->
                            Long.compare(b.getLastAccessedTime(), a.getLastAccessedTime()))
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    private void evictLRU() {
        System.out.println("--------------evict LRU Called------------------------------");
        String lruKey = cache.values()
                .stream()
                .min(Comparator.comparing(CacheEntry::getLastAccessedTime))
                .get()
                .getCacheKey();

        cache.remove(lruKey);
        repository.deleteById(lruKey);
    }

    public int getSize() {
        return cache.size();
    }
}


