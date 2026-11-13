package com.dappunq.dto.valuation;

import com.dappunq.model.valuation.*;
import java.math.BigDecimal;
import java.time.Instant;

public record ValuationResponse(Long usuarioId, Long jugadorId,
                                BigDecimal valorUnitario,
                                StrategyType tipoEstrategia, Instant ejecutadoEn,
                                ExecutionType tipoEjecucion) {

}
