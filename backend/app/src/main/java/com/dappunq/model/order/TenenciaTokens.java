package com.dappunq.model.order;

import com.dappunq.model.order.exception.CodigoErrorOrden;
import com.dappunq.model.order.exception.OrdenException;

public final class TenenciaTokens {
    private int cantidad;
    private int bloqueados;

    public TenenciaTokens(int cantidad, int bloqueados) {
        if (cantidad < 0 || bloqueados < 0 || bloqueados > cantidad) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "La cantidad de tokens bloqueados no puede superar la tenencia");
        }
        this.cantidad = cantidad;
        this.bloqueados = bloqueados;
    }

    public void reservar(int cantidad) {
        if (cantidad <= 0 || disponibles() < cantidad) {
            throw new OrdenException(CodigoErrorOrden.RECURSOS_INSUFICIENTES,
                    "Tokens disponibles insuficientes");
        }
        bloqueados = Math.addExact(bloqueados, cantidad);
    }

    public void liberar(int cantidad) {
        if (cantidad <= 0 || bloqueados < cantidad) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "No se puede liberar la cantidad de tokens solicitada");
        }
        bloqueados -= cantidad;
    }

    public void transferirBloqueados(int cantidad) {
        if (cantidad <= 0 || bloqueados < cantidad || this.cantidad < cantidad) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "La reserva de tokens no cubre la transferencia");
        }
        this.cantidad -= cantidad;
        bloqueados -= cantidad;
    }

    public void recibir(int cantidad) {
        if (cantidad <= 0 || cantidad > Integer.MAX_VALUE - this.cantidad) {
            throw new OrdenException(CodigoErrorOrden.ORDEN_INVALIDA,
                    "La cantidad recibida no es válida para la tenencia");
        }
        this.cantidad += cantidad;
    }

    public int disponibles() {
        return cantidad - bloqueados;
    }

    public int getCantidad() { return cantidad; }
    public int getBloqueados() { return bloqueados; }
}
