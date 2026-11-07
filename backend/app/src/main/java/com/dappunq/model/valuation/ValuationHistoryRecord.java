package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Objeto de dominio inmutable que representa un snapshot del historial de valuacion.
 * No contiene anotaciones de framework ni de persistencia.
 */
public class ValuationHistoryRecord {

    private final Long usuarioId;
    private final Long jugadorId;
    private final int tokens;
    private final BigDecimal valorUnitario;
    private final BigDecimal valorTotal;
    private final StrategyType estrategia;
    private final Instant ejecutadoEn;
    private final ExecutionType tipoEjecucion;

    public ValuationHistoryRecord(Long usuarioId, Long jugadorId, int tokens,
                                   BigDecimal valorUnitario, BigDecimal valorTotal,
                                   StrategyType estrategia, Instant ejecutadoEn,
                                   ExecutionType tipoEjecucion) {
        this.usuarioId = usuarioId;
        this.jugadorId = jugadorId;
        this.tokens = tokens;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.estrategia = estrategia;
        this.ejecutadoEn = ejecutadoEn;
        this.tipoEjecucion = tipoEjecucion;
    }

    public Long getUsuarioId() { return usuarioId; }
    public Long getJugadorId() { return jugadorId; }
    public int getTokens() { return tokens; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public StrategyType getEstrategia() { return estrategia; }
    public Instant getEjecutadoEn() { return ejecutadoEn; }
    public ExecutionType getTipoEjecucion() { return tipoEjecucion; }
}
