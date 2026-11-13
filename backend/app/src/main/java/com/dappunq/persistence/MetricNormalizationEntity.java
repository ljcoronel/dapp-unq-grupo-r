package com.dappunq.persistence;

import com.dappunq.model.valuation.Metric;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "metric_normalization")
public class MetricNormalizationEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "metric", nullable = false)
    private Metric metric;

    @Column(name = "minimum_value", nullable = false, precision = 19, scale = 8)
    private BigDecimal minimumValue;

    @Column(name = "maximum_value", nullable = false, precision = 19, scale = 8)
    private BigDecimal maximumValue;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    protected MetricNormalizationEntity() {
    }

    public MetricNormalizationEntity(Metric metric, BigDecimal minimumValue,
                                     BigDecimal maximumValue, Instant calculatedAt) {
        this.metric = metric;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.calculatedAt = calculatedAt;
    }

    public Metric getMetric() {
        return metric;
    }

    public BigDecimal getMinimumValue() {
        return minimumValue;
    }

    public BigDecimal getMaximumValue() {
        return maximumValue;
    }

    public Instant getCalculatedAt() {
        return calculatedAt;
    }
}
