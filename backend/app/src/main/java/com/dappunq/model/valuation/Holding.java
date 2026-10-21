package com.dappunq.model.valuation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Holding {

    private final Long usuarioId;
    private final Long jugadorId;
    private final Integer tokens;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;

    public Holding(Long usuarioId, Long jugadorId, Integer tokens, BigDecimal valorUnitario) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        this.jugadorId = Objects.requireNonNull(jugadorId, "jugadorId no puede ser nulo");
        if (tokens == null || tokens < 0) {
            throw new ValuationDomainException("La cantidad de tokens no puede ser negativa");
        }
        this.tokens = tokens;
        this.valorUnitario = valorUnitario != null
                ? valorUnitario.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(tokens)).setScale(2, RoundingMode.HALF_UP);
    }

    public Holding(Long usuarioId, Long jugadorId, Integer tokens) {
        this(usuarioId, jugadorId, tokens, BigDecimal.ZERO);
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getJugadorId() {
        return jugadorId;
    }

    public Integer getTokens() {
        return tokens;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void valuate(BigDecimal nuevoValorUnitario) {
        if (nuevoValorUnitario == null || nuevoValorUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValuationDomainException("El valor unitario no puede ser negativo");
        }
        this.valorUnitario = nuevoValorUnitario.setScale(2, RoundingMode.HALF_UP);
        this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(tokens)).setScale(2, RoundingMode.HALF_UP);
    }
}
