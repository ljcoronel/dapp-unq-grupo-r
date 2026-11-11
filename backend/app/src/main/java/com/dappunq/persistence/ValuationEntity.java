package com.dappunq.persistence;

import com.dappunq.model.valuation.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity @Table(name="valuaciones",
    uniqueConstraints=@UniqueConstraint(columnNames={"usuario_id","jugador_id","ventana","tipo_ejecucion"}))
public class ValuationEntity {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="usuario_id")
    UserEntity user;

    @ManyToOne(optional=false)
    @JoinColumn(name="jugador_id")
    PlayerEntity player;
    int tokens;

    @Column(name="valor_unitario")
    BigDecimal valorUnitario;

    @Column(name="valor_total")
    BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    StrategyType estrategia;

    Instant ejecutadoEn = Instant.now();
    String ventana;

    @Enumerated(EnumType.STRING)
    ExecutionType tipoEjecucion;

    protected ValuationEntity(){}
    public ValuationEntity(UserEntity user, PlayerEntity player, int tokens,
                           BigDecimal valorUnitario, StrategyType tipoEstrategia,
                           String ventana, ExecutionType tipoEjecucion) {
        this.user = user;
        this.player = player;
        this.tokens = tokens;
        this.valorUnitario = valorUnitario;
        valorTotal = valorUnitario.multiply(BigDecimal.valueOf(tokens));
        estrategia = tipoEstrategia;
        this.ventana = ventana;
        this.tipoEjecucion = tipoEjecucion;
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

    public int getTokens(){
        return tokens;
    }

    public BigDecimal getValorUnitario(){return valorUnitario;}
    public BigDecimal getValorTotal(){return valorTotal;}
    public StrategyType getEstrategia(){return estrategia;}
    public Instant getEjecutadoEn(){return ejecutadoEn;}
    public ExecutionType getTipoEjecucion(){return tipoEjecucion;} }
