package com.dappunq.persistence.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.persistence.PlayerEntity;
import com.dappunq.persistence.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ordenes", indexes = {
        @Index(name = "idx_ordenes_usuario_estado_fecha", columnList = "usuario_id, estado, fecha_emision"),
        @Index(name = "idx_ordenes_usuario_ejecucion", columnList = "usuario_id, fecha_ejecucion"),
        @Index(name = "idx_ordenes_matching", columnList = "jugador_id, cantidad, estado, tipo_operacion")
})
public class OrdenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserEntity usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 16)
    private TipoOperacionOrden tipoOperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private PlayerEntity jugador;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal tolerancia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EstadoOrden estado;

    @Column(name = "fecha_emision", nullable = false, updatable = false)
    private Instant fechaEmision;

    @Column(name = "fecha_ejecucion")
    private Instant fechaEjecucion;

    @Column(name = "fecha_cancelacion")
    private Instant fechaCancelacion;

    @Column(name = "precio_liquidado", precision = 15, scale = 2)
    private BigDecimal precioLiquidado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id")
    private UserEntity comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private UserEntity vendedor;

    protected OrdenEntity() {
    }

    public OrdenEntity(Orden orden, UserEntity usuario, PlayerEntity jugador) {
        this.usuario = usuario;
        this.jugador = jugador;
        this.tipoOperacion = orden.getTipoOperacion();
        this.cantidad = orden.getCantidad();
        this.precio = orden.getPrecio();
        this.tolerancia = orden.getTolerancia();
        this.estado = orden.getEstado();
        this.fechaEmision = orden.getFechaEmision();
        this.actualizarDesde(orden, null, null);
    }

    public void actualizarDesde(Orden orden, UserEntity comprador, UserEntity vendedor) {
        this.estado = orden.getEstado();
        this.fechaEjecucion = orden.getFechaEjecucion();
        this.fechaCancelacion = orden.getFechaCancelacion();
        this.precioLiquidado = orden.getPrecioLiquidado();
        this.comprador = comprador;
        this.vendedor = vendedor;
    }

    public Long getId() { return id; }
    public UserEntity getUsuario() { return usuario; }
    public TipoOperacionOrden getTipoOperacion() { return tipoOperacion; }
    public PlayerEntity getJugador() { return jugador; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecio() { return precio; }
    public BigDecimal getTolerancia() { return tolerancia; }
    public EstadoOrden getEstado() { return estado; }
    public Instant getFechaEmision() { return fechaEmision; }
    public Instant getFechaEjecucion() { return fechaEjecucion; }
    public Instant getFechaCancelacion() { return fechaCancelacion; }
    public BigDecimal getPrecioLiquidado() { return precioLiquidado; }
    public UserEntity getComprador() { return comprador; }
    public UserEntity getVendedor() { return vendedor; }
}
