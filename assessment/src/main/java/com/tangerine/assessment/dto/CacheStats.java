package com.tangerine.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CacheStats {
    private int currentSize;
    private int capacity;
}
