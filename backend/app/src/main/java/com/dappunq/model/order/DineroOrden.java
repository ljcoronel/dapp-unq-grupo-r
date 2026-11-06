package com.dappunq.model.order;

import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record DineroOrden(BigDecimal valor) {
    public DineroOrden {
        if (valor == null || valor.signum() < 0 || valor.scale() > 2) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "El importe debe ser no negativo y tener como máximo dos decimales");
        }
        valor = valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    public DineroOrden sumar(DineroOrden otro) {
        return new DineroOrden(valor.add(otro.valor));
    }

    public DineroOrden restar(DineroOrden otro) {
        return new DineroOrden(valor.subtract(otro.valor));
    }
}
