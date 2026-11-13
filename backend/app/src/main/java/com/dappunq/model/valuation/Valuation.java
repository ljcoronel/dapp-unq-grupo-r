package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Objeto de dominio inmutable que representa un snapshot del historial de valuacion.
 * No contiene anotaciones de framework ni de persistencia.
 */
public class Valuation {

    private final Long usuarioId;
    private final Long jugadorId;
    private final BigDecimal valorUnitario;
    private final StrategyType tipoEstrategia;
    private final Instant ejecutadoEn;
    private final ExecutionType tipoEjecucion;

    public Valuation(Long usuarioId, Long jugadorId,
                     BigDecimal valorUnitario,
                     StrategyType tipoEstrategia, Instant ejecutadoEn,
                     ExecutionType tipoEjecucion) {
        this.usuarioId = usuarioId;
        this.jugadorId = jugadorId;
        this.valorUnitario = valorUnitario;
        this.tipoEstrategia = tipoEstrategia;
        this.ejecutadoEn = ejecutadoEn;
        this.tipoEjecucion = tipoEjecucion;
    }

    public Long getUsuarioId() { return usuarioId; }
    public Long getJugadorId() { return jugadorId; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public StrategyType getTipoEstrategia() { return tipoEstrategia; }
    public Instant getEjecutadoEn() { return ejecutadoEn; }
    public ExecutionType getTipoEjecucion() { return tipoEjecucion; }
}
