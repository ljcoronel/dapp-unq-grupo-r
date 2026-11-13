package com.dappunq.persistence;

import com.dappunq.model.valuation.Holding;
import com.dappunq.model.valuation.Metric;
import com.dappunq.model.valuation.NormalizationRange;
import com.dappunq.model.valuation.PlayerSnapshot;
import com.dappunq.model.valuation.ValuationDomainException;
import com.dappunq.model.valuation.Valuation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ValuationPersistenceMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public String write(Map<Metric, BigDecimal> m) {
        try {
            return mapper.writeValueAsString(m);
        } catch (Exception e) {
            throw new ValuationDomainException("Pesos invalidos");
        }
    }

    public Map<Metric, BigDecimal> read(String s) {
        try {
            return mapper.readValue(s, new TypeReference<Map<Metric, BigDecimal>>() {});
        } catch (Exception e) {
            throw new ValuationDomainException("Pesos invalidos");
        }
    }

    public PlayerSnapshot toSnapshot(PlayerEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PlayerSnapshot(
                entity.getId(),
                entity.getPosition(),
                entity.getGoals(),
                entity.getAssists(),
                entity.getPassSuccessPercentage(),
                entity.getAerialsWon(),
                entity.getTimesManOfTheMatch(),
                entity.getMinutesPlayed(),
                entity.getAppearances()
        );
    }

    public NormalizationRange toNormalizationRange(MetricNormalizationEntity entity) {
        return new NormalizationRange(entity.getMetric(), entity.getMinimumValue(), entity.getMaximumValue());
    }

    public Holding toModel(HoldingEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Holding(
                entity.getId(),
                entity.getUser().getId(),
                entity.getPlayer().getId(),
                entity.getTokens(),
                entity.getValorUnitario()
        );
    }

    public Valuation toModel(ValuationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Valuation(
            entity.getUser().getId(),
            entity.getPlayer().getId(),
            entity.getValorUnitario(),
            entity.getEstrategia(),
            entity.getEjecutadoEn(),
            entity.getTipoEjecucion()
        );
    }
}
