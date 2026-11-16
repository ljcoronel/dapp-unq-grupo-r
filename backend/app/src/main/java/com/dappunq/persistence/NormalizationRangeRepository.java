package com.dappunq.persistence;

import com.dappunq.model.valuation.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface NormalizationRangeRepository extends JpaRepository<MetricNormalizationEntity, Metric> {

    @Query("select r from MetricNormalizationEntity r")
    List<MetricNormalizationEntity> findCurrentRanges();

    default void replaceCurrentRanges(Collection<MetricNormalizationEntity> ranges) {
        deleteAllInBatch();
        saveAll(ranges);
    }
}
