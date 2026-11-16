package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Objeto de dominio que representa el estado de configuracion de tipo de un usuario.
 * No contiene anotaciones de framework ni de persistencia.
 */
public class StrategyState {

    private final StrategyStatus estadoActual;
    private final StrategyType tipoEstrategiaActual;
    private final Map<Metric, BigDecimal> pesosActual;
    private final Instant vigenteDesdeActual;
    private final StrategyStatus estadoPendiente;
    private final StrategyType tipoEstrategiaPendiente;
    private final Map<Metric, BigDecimal> pesosPendiente;
    private final Instant vigenteDesdePendiente;

    public StrategyState(StrategyStatus estadoActual,
                         StrategyType tipoEstrategiaActual, Map<Metric, BigDecimal> pesosActual, Instant vigenteDesdeActual,
                         StrategyStatus estadoPendiente,
                         StrategyType tipoEstrategiaPendiente, Map<Metric, BigDecimal> pesosPendiente, Instant vigenteDesdePendiente) {
        this.estadoActual = estadoActual;
        this.tipoEstrategiaActual = tipoEstrategiaActual;
        this.pesosActual = pesosActual;
        this.vigenteDesdeActual = vigenteDesdeActual;
        this.estadoPendiente = estadoPendiente;
        this.tipoEstrategiaPendiente = tipoEstrategiaPendiente;
        this.pesosPendiente = pesosPendiente;
        this.vigenteDesdePendiente = vigenteDesdePendiente;
    }

    public StrategyStatus getEstadoActual() { return estadoActual; }
    public StrategyType getTipoEstrategiaActual() { return tipoEstrategiaActual; }
    public Map<Metric, BigDecimal> getPesosActual() { return pesosActual; }
    public Instant getVigenteDesdeActual() { return vigenteDesdeActual; }
    public StrategyStatus getEstadoPendiente() { return estadoPendiente; }
    public StrategyType getTipoEstrategiaPendiente() { return tipoEstrategiaPendiente; }
    public Map<Metric, BigDecimal> getPesosPendiente() { return pesosPendiente; }
    public Instant getVigenteDesdePendiente() { return vigenteDesdePendiente; }

    public StrategyStatus estado() { return estadoActual; }
}
