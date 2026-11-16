package com.dappunq.model.valuation;

import java.math.BigDecimal;

public record NormalizationRange(Metric metric, BigDecimal minimum, BigDecimal maximum) {
    public NormalizationRange {
        if (metric == null || minimum == null || maximum == null) {
            throw new IllegalArgumentException("Un rango de normalizacion requiere metrica y limites");
        }
        if (minimum.compareTo(maximum) > 0) {
            throw new IllegalArgumentException("El minimo no puede ser mayor que el maximo");
        }
    }
}
