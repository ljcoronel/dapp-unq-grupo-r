package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Objeto de dominio que representa el estado de configuracion de estrategia de un usuario.
 * No contiene anotaciones de framework ni de persistencia.
 */
public class StrategyState {

    private final StrategyType estrategia;
    private final Map<String, BigDecimal> pesos;
    private final StrategyStatus estado;
    private final Instant vigenteDesde;
    private final StrategyType estrategiaActual;
    private final Map<String, BigDecimal> pesosActual;
    private final Instant vigenteDesdeActual;
    private final StrategyType estrategiaPendiente;
    private final Map<String, BigDecimal> pesosPendiente;
    private final Instant vigentesDesdePendiente;

    public StrategyState(StrategyType estrategia, Map<String, BigDecimal> pesos,
                         StrategyStatus estado, Instant vigenteDesde,
                         StrategyType estrategiaActual, Map<String, BigDecimal> pesosActual, Instant vigenteDesdeActual,
                         StrategyType estrategiaPendiente, Map<String, BigDecimal> pesosPendiente, Instant vigentesDesdePendiente) {
        this.estrategia = estrategia;
        this.pesos = pesos;
        this.estado = estado;
        this.vigenteDesde = vigenteDesde;
        this.estrategiaActual = estrategiaActual;
        this.pesosActual = pesosActual;
        this.vigenteDesdeActual = vigenteDesdeActual;
        this.estrategiaPendiente = estrategiaPendiente;
        this.pesosPendiente = pesosPendiente;
        this.vigentesDesdePendiente = vigentesDesdePendiente;
    }

    public StrategyType getEstrategia() { return estrategia; }
    public Map<String, BigDecimal> getPesos() { return pesos; }
    public StrategyStatus getEstado() { return estado; }
    public Instant getVigenteDesde() { return vigenteDesde; }
    public StrategyType getEstrategiaActual() { return estrategiaActual; }
    public Map<String, BigDecimal> getPesosActual() { return pesosActual; }
    public Instant getVigenteDesdeActual() { return vigenteDesdeActual; }
    public StrategyType getEstrategiaPendiente() { return estrategiaPendiente; }
    public Map<String, BigDecimal> getPesosPendiente() { return pesosPendiente; }
    public Instant getVigentesDesdePendiente() { return vigentesDesdePendiente; }
}
