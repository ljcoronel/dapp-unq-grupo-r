package com.dappunq.controller;

import com.dappunq.dto.valuation.ValuationResponse;
import com.dappunq.dto.valuation.HoldingResponse;
import com.dappunq.dto.valuation.RecalculationResult;
import com.dappunq.dto.valuation.StrategyConfigDto;
import com.dappunq.dto.valuation.StrategyStateResponse;
import com.dappunq.model.valuation.Holding;
import com.dappunq.model.valuation.RecalculationSummary;
import com.dappunq.model.valuation.StrategyState;
import com.dappunq.model.valuation.Valuation;
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
        StrategyConfigDto actualDto = (state.getTipoEstrategiaActual() != null)
                ? new StrategyConfigDto(state.getTipoEstrategiaActual(), state.getPesosActual(), state.getVigenteDesdeActual())
                : null;
        StrategyConfigDto pendienteDto = (state.getTipoEstrategiaPendiente() != null)
                ? new StrategyConfigDto(state.getTipoEstrategiaPendiente(), state.getPesosPendiente(), state.getVigenteDesdePendiente())
                : null;
        return new StrategyStateResponse(
                state.getTipoEstrategiaActual(),
                state.getPesosActual(),
                state.getEstadoActual(),
                state.getVigenteDesdeActual(),
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

    public ValuationResponse toResponse(Valuation r) {
        if (r == null) {
            return null;
        }
        return new ValuationResponse(
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
        return new StrategyStateResponse(e.getTipoEstrategia(), weights, e.getEstado(), e.getVigenteDesde());
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
