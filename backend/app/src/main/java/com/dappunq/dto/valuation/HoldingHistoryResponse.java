package com.dappunq.dto.valuation; import com.dappunq.model.valuation.*; import java.math.*; import java.time.*;
public record HoldingHistoryResponse(Long usuarioId,Long jugadorId,int tokens,BigDecimal valorUnitario,BigDecimal valorTotal,StrategyType estrategia,Instant ejecutadoEn,ExecutionType tipoEjecucion) {}
