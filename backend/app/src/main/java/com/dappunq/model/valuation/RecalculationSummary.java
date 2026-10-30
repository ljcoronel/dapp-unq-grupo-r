package com.dappunq.model.valuation;

import java.time.Instant;
import java.util.Objects;

/**
 * Objeto de dominio inmutable que representa el resumen de una ejecucion de valuacion.
 * Cumple con Constitution II y III al residir en la capa Model como POJO
 * sin dependencias de framework ni anotaciones de persistencia.
 */
public class RecalculationSummary {

    private final String status;
    private final int playersScraped;
    private final int recalculatedHoldings;
    private final Instant executedAt;

    public RecalculationSummary(String status, int playersScraped, int recalculatedHoldings, Instant executedAt) {
        if (status == null || status.isBlank()) {
            throw new ValuationDomainException("El estado de la recalculacion no puede ser nulo ni vacio");
        }
        if (playersScraped < 0) {
            throw new ValuationDomainException("La cantidad de jugadores scrapeados no puede ser negativa");
        }
        if (recalculatedHoldings < 0) {
            throw new ValuationDomainException("La cantidad de tenencias recalculadas no puede ser negativa");
        }
        this.status = status;
        this.playersScraped = playersScraped;
        this.recalculatedHoldings = recalculatedHoldings;
        this.executedAt = Objects.requireNonNull(executedAt, "La fecha de ejecucion no puede ser nula");
    }

    public String getStatus() {
        return status;
    }

    public int getPlayersScraped() {
        return playersScraped;
    }

    public int getRecalculatedHoldings() {
        return recalculatedHoldings;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public String status() {
        return status;
    }

    public int playersScraped() {
        return playersScraped;
    }

    public int recalculatedHoldings() {
        return recalculatedHoldings;
    }

    public Instant executedAt() {
        return executedAt;
    }
}
