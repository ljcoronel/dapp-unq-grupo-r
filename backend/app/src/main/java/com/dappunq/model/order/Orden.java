package com.dappunq.model.order;

import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;

import java.math.BigDecimal;
import java.time.Instant;

public final class Orden {
    private final Long id;
    private final Long usuarioId;
    private final TipoOperacionOrden tipoOperacion;
    private final Long jugadorId;
    private final CantidadTokens cantidad;
    private final DineroOrden precio;
    private final DineroOrden tolerancia;
    private final Instant fechaEmision;
    private EstadoOrden estado;
    private Instant fechaEjecucion;
    private Instant fechaCancelacion;
    private BigDecimal precioLiquidado;
    private Long compradorId;
    private Long vendedorId;

    private Orden(Long id, Long usuarioId, TipoOperacionOrden tipoOperacion, Long jugadorId,
                  CantidadTokens cantidad, DineroOrden precio, DineroOrden tolerancia,
                  EstadoOrden estado, Instant fechaEmision, Instant fechaEjecucion,
                  Instant fechaCancelacion, BigDecimal precioLiquidado,
                  Long compradorId, Long vendedorId) {
        if (usuarioId == null || usuarioId <= 0 || jugadorId == null || jugadorId <= 0
                || tipoOperacion == null || estado == null || fechaEmision == null) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA, "Los datos de la orden son obligatorios");
        }
        if (tipoOperacion == TipoOperacionOrden.COMPRA && cantidad.valor() > 100) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "Una orden de compra no puede superar los 100 tokens");
        }
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipoOperacion = tipoOperacion;
        this.jugadorId = jugadorId;
        this.cantidad = cantidad;
        this.precio = precio;
        this.tolerancia = tolerancia;
        this.estado = estado;
        this.fechaEmision = fechaEmision;
        this.fechaEjecucion = fechaEjecucion;
        this.fechaCancelacion = fechaCancelacion;
        this.precioLiquidado = precioLiquidado;
        this.compradorId = compradorId;
        this.vendedorId = vendedorId;
    }

    public static Orden crear(Long usuarioId, TipoOperacionOrden tipoOperacion, Long jugadorId,
                              Integer cantidad, BigDecimal precio, BigDecimal tolerancia) {
        if (cantidad == null) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA, "La cantidad de tokens es obligatoria");
        }
        return new Orden(null, usuarioId, tipoOperacion, jugadorId, new CantidadTokens(cantidad),
                new DineroOrden(precio), new DineroOrden(tolerancia), EstadoOrden.PENDIENTE,
                Instant.now(), null, null, null, null, null);
    }

    public static Orden rehidratar(Long id, Long usuarioId, TipoOperacionOrden tipoOperacion,
                                   Long jugadorId, Integer cantidad, BigDecimal precio,
                                   BigDecimal tolerancia, EstadoOrden estado, Instant fechaEmision,
                                   Instant fechaEjecucion, Instant fechaCancelacion,
                                   BigDecimal precioLiquidado, Long compradorId, Long vendedorId) {
        return new Orden(id, usuarioId, tipoOperacion, jugadorId, new CantidadTokens(cantidad),
                new DineroOrden(precio), new DineroOrden(tolerancia), estado, fechaEmision,
                fechaEjecucion, fechaCancelacion, precioLiquidado, compradorId, vendedorId);
    }

    public Orden conId(Long nuevoId) {
        return rehidratar(nuevoId, usuarioId, tipoOperacion, jugadorId, cantidad.valor(),
                precio.valor(), tolerancia.valor(), estado, fechaEmision, fechaEjecucion,
                fechaCancelacion, precioLiquidado, compradorId, vendedorId);
    }

    public BigDecimal precioMinimo() {
        return tipoOperacion == TipoOperacionOrden.COMPRA
                ? precio.valor()
                : precio.valor().subtract(tolerancia.valor()).max(BigDecimal.ZERO);
    }

    public BigDecimal precioMaximo() {
        return tipoOperacion == TipoOperacionOrden.COMPRA
                ? precio.valor().add(tolerancia.valor())
                : precio.valor();
    }

    public boolean compatibleCon(Orden otra) {
        return otra != null
                && estado == EstadoOrden.PENDIENTE
                && otra.estado == EstadoOrden.PENDIENTE
                && !usuarioId.equals(otra.usuarioId)
                && tipoOperacion != otra.tipoOperacion
                && jugadorId.equals(otra.jugadorId)
                && cantidad.equals(otra.cantidad)
                && precioMinimo().max(otra.precioMinimo()).compareTo(
                        precioMaximo().min(otra.precioMaximo())) <= 0;
    }

    public static BigDecimal precioDeLiquidacion(Orden ordenBase, Orden contraparte) {
        if (!ordenBase.compatibleCon(contraparte)) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "Las órdenes no son compatibles");
        }
        BigDecimal base = ordenBase.precio.valor();
        if (perteneceAlRango(base, ordenBase) && perteneceAlRango(base, contraparte)) {
            return base.setScale(2);
        }
        return ordenBase.precioMinimo().max(contraparte.precioMinimo()).setScale(2);
    }

    private static boolean perteneceAlRango(BigDecimal precio, Orden orden) {
        return precio.compareTo(orden.precioMinimo()) >= 0
                && precio.compareTo(orden.precioMaximo()) <= 0;
    }

    public void ejecutar(Long compradorId, Long vendedorId, BigDecimal precioFinal, Instant fecha) {
        exigirPendiente();
        boolean propietarioParticipa = tipoOperacion == TipoOperacionOrden.COMPRA
                ? usuarioId.equals(compradorId) : usuarioId.equals(vendedorId);
        if (compradorId == null || vendedorId == null || compradorId.equals(vendedorId)
                || !propietarioParticipa
                || fecha == null || precioFinal == null || precioFinal.scale() > 2
                || precioFinal.compareTo(precioMinimo()) < 0
                || precioFinal.compareTo(precioMaximo()) > 0) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "Los datos de liquidación de la orden no son válidos");
        }
        estado = EstadoOrden.EJECUTADA;
        fechaEjecucion = fecha;
        precioLiquidado = precioFinal.setScale(2);
        this.compradorId = compradorId;
        this.vendedorId = vendedorId;
    }

    public void cancelar(Long solicitanteId, Instant fecha) {
        exigirPendiente();
        if (!usuarioId.equals(solicitanteId)) {
            throw new OrdenException(CodigoErrorOrden.PROPIETARIO_INVALIDO,
                    "Solo el propietario puede cancelar la orden");
        }
        if (fecha == null) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "La fecha de cancelación es obligatoria");
        }
        estado = EstadoOrden.CANCELADA;
        fechaCancelacion = fecha;
    }

    private void exigirPendiente() {
        if (estado != EstadoOrden.PENDIENTE) {
            throw new OrdenException(CodigoErrorOrden.ESTADO_ORDEN_INVALIDO,
                    "Solo se pueden modificar órdenes pendientes");
        }
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public TipoOperacionOrden getTipoOperacion() { return tipoOperacion; }
    public Long getJugadorId() { return jugadorId; }
    public int getCantidad() { return cantidad.valor(); }
    public BigDecimal getPrecio() { return precio.valor(); }
    public BigDecimal getTolerancia() { return tolerancia.valor(); }
    public EstadoOrden getEstado() { return estado; }
    public Instant getFechaEmision() { return fechaEmision; }
    public Instant getFechaEjecucion() { return fechaEjecucion; }
    public Instant getFechaCancelacion() { return fechaCancelacion; }
    public BigDecimal getPrecioLiquidado() { return precioLiquidado; }
    public Long getCompradorId() { return compradorId; }
    public Long getVendedorId() { return vendedorId; }
}
