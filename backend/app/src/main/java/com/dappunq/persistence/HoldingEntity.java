package com.dappunq.persistence;

import com.dappunq.model.valuation.ValuationDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "tenencias", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "jugador_id"}))
public class HoldingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private UserEntity user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "jugador_id")
    private PlayerEntity player;

    @Column(nullable = false)
    private Integer tokens;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    protected HoldingEntity() {}

    public HoldingEntity(UserEntity user, PlayerEntity player, Integer tokens) {
        if (tokens == null || tokens < 0) {
            throw new ValuationDomainException("La cantidad de tokens no puede ser negativa");
        }
        this.user = user;
        this.player = player;
        this.tokens = tokens;
        this.valorUnitario = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.valorTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public HoldingEntity(UserEntity user, PlayerEntity player, Integer tokens, BigDecimal valorUnitario) {
        if (tokens == null || tokens < 0) {
            throw new ValuationDomainException("La cantidad de tokens no puede ser negativa");
        }
        this.user = user;
        this.player = player;
        this.tokens = tokens;
        this.valorUnitario = valorUnitario != null ? valorUnitario.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(tokens)).setScale(2, RoundingMode.HALF_UP);
    }

    @PrePersist
    @PreUpdate
    protected void ensureInvariants() {
        if (tokens != null && valorUnitario != null) {
            this.valorUnitario = this.valorUnitario.setScale(2, RoundingMode.HALF_UP);
            this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(tokens)).setScale(2, RoundingMode.HALF_UP);
        }
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public Integer getTokens() {
        return tokens;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void valuate(BigDecimal v) {
        if (v == null || v.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValuationDomainException("El valor unitario no puede ser negativo");
        }
        this.valorUnitario = v.setScale(2, RoundingMode.HALF_UP);
        this.valorTotal = this.valorUnitario.multiply(BigDecimal.valueOf(tokens)).setScale(2, RoundingMode.HALF_UP);
    }
}
