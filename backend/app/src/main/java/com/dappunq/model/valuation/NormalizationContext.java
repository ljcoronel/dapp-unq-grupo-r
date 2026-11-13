package com.dappunq.model.valuation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record NormalizationContext(Map<Metric, NormalizationRange> ranges) {
    public NormalizationContext {
        EnumMap<Metric, NormalizationRange> copy = new EnumMap<>(Metric.class);
        if (ranges != null) {
            copy.putAll(ranges);
        }
        ranges = Collections.unmodifiableMap(copy);
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }
}
