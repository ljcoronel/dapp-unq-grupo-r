package com.dappunq.unit.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.model.order.exception.OrdenException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdenCancelacionTest {
    @Test
    void soloElPropietarioPuedeCancelarUnaOrdenPendienteUnaVez() {
        Orden orden = Orden.crear(7L, TipoOperacionOrden.COMPRA, 8L, 2,
                new BigDecimal("4.25"), new BigDecimal("0.75"));

        assertThrows(OrdenException.class, () -> orden.cancelar(8L, Instant.now()));
        orden.cancelar(7L, Instant.now());
        assertEquals(EstadoOrden.CANCELADA, orden.getEstado());
        assertNull(orden.getFechaEjecucion());
        assertThrows(OrdenException.class, () -> orden.cancelar(7L, Instant.now()));
    }
}
