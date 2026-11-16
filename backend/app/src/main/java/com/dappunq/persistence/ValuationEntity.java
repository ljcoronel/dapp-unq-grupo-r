package com.dappunq.persistence;

import com.dappunq.model.valuation.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity @Table(name="valuaciones")
public class ValuationEntity {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="usuario_id")
    UserEntity user;

    @ManyToOne(optional=false)
    @JoinColumn(name="jugador_id")
    PlayerEntity player;

    @Column(name="valor_unitario")
    BigDecimal valorUnitario;

    @Enumerated(EnumType.STRING)
    StrategyType estrategia;

    Instant ejecutadoEn = Instant.now();

    @Enumerated(EnumType.STRING)
    ExecutionType tipoEjecucion;

    @Column(name = "is_current")
    boolean isCurrent;

    protected ValuationEntity(){}
    public ValuationEntity(UserEntity user, PlayerEntity player,
                           BigDecimal valorUnitario, StrategyType tipoEstrategia,
                           ExecutionType tipoEjecucion) {
        this.user = user;
        this.player = player;
        this.valorUnitario = valorUnitario;
        estrategia = tipoEstrategia;
        this.tipoEjecucion = tipoEjecucion;
        this.isCurrent = true;
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

    public BigDecimal getValorUnitario(){return valorUnitario;}
    public StrategyType getEstrategia(){return estrategia;}
    public Instant getEjecutadoEn(){return ejecutadoEn;}
    public ExecutionType getTipoEjecucion(){return tipoEjecucion;}
    public void setIsCurrent(boolean current) {
        isCurrent = current;
    }
}
