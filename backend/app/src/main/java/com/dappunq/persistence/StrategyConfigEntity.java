package com.dappunq.persistence;

import com.dappunq.model.valuation.StrategyStatus;
import com.dappunq.model.valuation.StrategyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "configuraciones_estrategia")
public class StrategyConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private StrategyType estrategia;

    @Column(nullable = false, length = 2000)
    private String pesos;

    @Enumerated(EnumType.STRING)
    private StrategyStatus estado;

    @Column(name = "vigente_desde")
    private Instant vigenteDesde;

    protected StrategyConfigEntity() {}

    public StrategyConfigEntity(UserEntity user, StrategyType estrategia, String pesos, StrategyStatus estado, Instant vigenteDesde) {
        this.user = user;
        this.estrategia = estrategia;
        this.pesos = pesos;
        this.estado = estado;
        this.vigenteDesde = vigenteDesde;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public StrategyType getEstrategia() {
        return estrategia;
    }

    public String getPesos() {
        return pesos;
    }

    public StrategyStatus getEstado() {
        return estado;
    }

    public Instant getVigenteDesde() {
        return vigenteDesde;
    }

    public void setEstado(StrategyStatus estado) {
        this.estado = estado;
    }

    public void setVigenteDesde(Instant vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }
}
