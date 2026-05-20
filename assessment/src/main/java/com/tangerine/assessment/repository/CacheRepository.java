package com.tangerine.assessment.repository;

import com.tangerine.assessment.entity.CacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CacheRepository extends JpaRepository<CacheEntry, String> {

    List<CacheEntry> findAllByOrderByLastAccessedTimeAsc();
}
