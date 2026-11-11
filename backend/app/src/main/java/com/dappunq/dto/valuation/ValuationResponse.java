package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.*;
import java.math.BigDecimal;
import java.time.Instant;

public record ValuationResponse(Long usuarioId, Long jugadorId, int tokens,
                                BigDecimal valorUnitario, BigDecimal valorTotal,
                                StrategyType estrategia, Instant ejecutadoEn,
                                ExecutionType tipoEjecucion) {

}
