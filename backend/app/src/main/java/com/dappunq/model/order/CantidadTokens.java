package com.dappunq.model.order;

import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;

public record CantidadTokens(int valor) {
    public CantidadTokens {
        if (valor <= 0) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "La cantidad de tokens debe ser un entero positivo");
        }
    }
}
