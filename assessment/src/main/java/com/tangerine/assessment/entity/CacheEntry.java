package com.tangerine.assessment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "cache_entry")
@Data
public class CacheEntry {

    @Id
    private String cacheKey;

    private String cacheValue;

    private Long lastAccessedTime;
}
