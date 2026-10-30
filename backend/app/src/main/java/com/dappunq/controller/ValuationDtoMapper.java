package com.dappunq.controller;

import com.dappunq.dto.valuation.HoldingHistoryResponse;
import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.dto.valuation.StrategyConfigDto;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.model.valuation.Holding;
import com.dappunq.model.valuation.RecalculationSummary;
import com.dappunq.model.valuation.StrategyState;
import com.dappunq.model.valuation.ValuationHistoryRecord;
import com.dappunq.persistence.HoldingEntity;
import com.dappunq.persistence.StrategyConfigEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class ValuationDtoMapper {

    public StrategyStateResponse toResponse(StrategyState state) {
        if (state == null) {
            return null;
        }
        StrategyConfigDto actualDto = (state.getEstrategiaActual() != null)
                ? new StrategyConfigDto(state.getEstrategiaActual(), state.getPesosActual(), state.getVigenteDesdeActual())
                : null;
        StrategyConfigDto pendienteDto = (state.getEstrategiaPendiente() != null)
                ? new StrategyConfigDto(state.getEstrategiaPendiente(), state.getPesosPendiente(), state.getVigentesDesdePendiente())
                : null;
        return new StrategyStateResponse(
                state.getEstrategia(),
                state.getPesos(),
                state.getEstado(),
                state.getVigenteDesde(),
                actualDto,
                pendienteDto
        );
    }

    public HoldingResponse toResponse(Holding h) {
        if (h == null) {
            return null;
        }
        return new HoldingResponse(
                h.getId(),
                h.getUsuarioId(),
                h.getJugadorId(),
                h.getTokens(),
                h.getValorTotal(),
                h.getValorUnitario()
        );
    }

    public HoldingHistoryResponse toResponse(ValuationHistoryRecord r) {
        if (r == null) {
            return null;
        }
        return new HoldingHistoryResponse(
                r.getUsuarioId(),
                r.getJugadorId(),
                r.getTokens(),
                r.getValorUnitario(),
                r.getValorTotal(),
                r.getEstrategia(),
                r.getEjecutadoEn(),
                r.getTipoEjecucion()
        );
    }

    public RecalculationResult toResponse(RecalculationSummary summary) {
        if (summary == null) {
            return null;
        }
        return new RecalculationResult(
                summary.getStatus(),
                summary.getPlayersScraped(),
                summary.getRecalculatedHoldings(),
                summary.getExecutedAt()
        );
    }

    public StrategyStateResponse strategy(StrategyConfigEntity e, Map<String, BigDecimal> weights) {
        return new StrategyStateResponse(e.getEstrategia(), weights, e.getEstado(), e.getVigenteDesde());
    }

    public HoldingResponse holding(HoldingEntity h) {
        return new HoldingResponse(
                h.getId(),
                h.getUser().getId(),
                h.getPlayer().getId(),
                h.getTokens(),
                h.getValorTotal(),
                h.getValorUnitario()
        );
    }
}
