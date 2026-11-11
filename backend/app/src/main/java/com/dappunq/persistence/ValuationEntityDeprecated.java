package com.dappunq.persistence;

import com.dappunq.model.valuation.*;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;

@Entity @Table(name="valuaciones",
    uniqueConstraints=@UniqueConstraint(columnNames={"usuario_id","jugador_id","ventana_dominical"}))
public class ValuationEntityDeprecated {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    Long id;

    @ManyToOne(optional=false) @JoinColumn(name="usuario_id")
    UserEntity user;

    @ManyToOne(optional=false) @JoinColumn(name="jugador_id")
    PlayerEntity player;

    @Column(name="ventana_dominical") String ventana;

    @Enumerated(EnumType.STRING)
    StrategyType estrategia;

    @Column(name="valor_unitario")
    BigDecimal valorUnitario;

    @Column(name="creado_en")
    Instant creadoEn=Instant.now();

//    @Column(name="is_current")
//    boolean is_current;

    protected ValuationEntityDeprecated(){}

    public ValuationEntityDeprecated(UserEntity user, PlayerEntity player, String ventana,
                                     StrategyType tipoEstrategia, BigDecimal valorUnitario) {
        this.user = user;
        this.player = player;
        this.ventana = ventana;
        this.estrategia = tipoEstrategia;
        this.valorUnitario = valorUnitario;
    }
}
