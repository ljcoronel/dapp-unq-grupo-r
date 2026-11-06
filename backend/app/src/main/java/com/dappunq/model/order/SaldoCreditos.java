package com.dappunq.model.order;

import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SaldoCreditos {
    private BigDecimal disponibles;
    private BigDecimal bloqueados;

    public SaldoCreditos(BigDecimal disponibles, BigDecimal bloqueados) {
        this.disponibles = validar(disponibles);
        this.bloqueados = validar(bloqueados);
    }

    public void reservar(BigDecimal importe) {
        importe = validar(importe);
        if (disponibles.compareTo(importe) < 0) {
            throw new OrdenException(CodigoErrorOrden.RECURSOS_INSUFICIENTES,
                    "Créditos disponibles insuficientes");
        }
        disponibles = disponibles.subtract(importe);
        bloqueados = bloqueados.add(importe);
    }

    public void liberar(BigDecimal importe) {
        importe = validar(importe);
        if (bloqueados.compareTo(importe) < 0) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "No se puede liberar el importe de créditos solicitado");
        }
        bloqueados = bloqueados.subtract(importe);
        disponibles = disponibles.add(importe);
    }

    public void liquidarCompra(BigDecimal importeReservado, BigDecimal importeLiquidado) {
        importeReservado = validar(importeReservado);
        importeLiquidado = validar(importeLiquidado);
        if (importeReservado.compareTo(importeLiquidado) < 0
                || bloqueados.compareTo(importeReservado) < 0) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "La reserva de créditos no cubre la liquidación");
        }
        bloqueados = bloqueados.subtract(importeReservado);
        disponibles = disponibles.add(importeReservado.subtract(importeLiquidado));
    }

    public void acreditar(BigDecimal importe) {
        disponibles = disponibles.add(validar(importe));
    }

    private static BigDecimal validar(BigDecimal importe) {
        if (importe == null || importe.signum() < 0 || importe.scale() > 2) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "El saldo debe ser no negativo y tener como máximo dos decimales");
        }
        return importe.setScale(2, RoundingMode.UNNECESSARY);
    }

    public BigDecimal getDisponibles() { return disponibles; }
    public BigDecimal getBloqueados() { return bloqueados; }
}
