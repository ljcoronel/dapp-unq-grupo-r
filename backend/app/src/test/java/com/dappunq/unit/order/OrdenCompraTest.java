package com.dappunq.unit.order;

import com.dappunq.model.order.EstadoOrden;
import com.dappunq.model.order.Orden;
import com.dappunq.model.order.TipoOperacionOrden;
import com.dappunq.model.order.exception.OrdenException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenCompraTest {
    @Test
    void compraAceptaExtremosInclusivosYLiquidaAlPrecioBaseCuandoCoincide() {
        Orden compra = Orden.crear(1L, TipoOperacionOrden.COMPRA, 9L, 20,
                new BigDecimal("20.00"), new BigDecimal("2.00"));
        Orden venta = Orden.crear(2L, TipoOperacionOrden.VENTA, 9L, 20,
                new BigDecimal("22.00"), BigDecimal.ZERO);

        assertTrue(compra.compatibleCon(venta));
        assertEquals(0, Orden.precioDeLiquidacion(compra, venta).compareTo(new BigDecimal("22.00")));
        compra.ejecutar(compra.getUsuarioId(), venta.getUsuarioId(), new BigDecimal("22.00"), Instant.now());
        assertEquals(EstadoOrden.EJECUTADA, compra.getEstado());
        assertEquals(0, compra.getPrecioLiquidado().compareTo(new BigDecimal("22.00")));
    }

    @Test
    void compraEscogeElMenorPrecioDeLaInterseccionCuandoElPrecioBaseNoCoincide() {
        Orden compra = Orden.crear(1L, TipoOperacionOrden.COMPRA, 9L, 3,
                new BigDecimal("12.00"), new BigDecimal("2.00"));
        Orden venta = Orden.crear(2L, TipoOperacionOrden.VENTA, 9L, 3,
                new BigDecimal("15.00"), new BigDecimal("2.00"));

        assertEquals(0, Orden.precioDeLiquidacion(compra, venta).compareTo(new BigDecimal("13.00")));
    }

    @Test
    void compraRechazaCantidadMayorACienYTransicionInvalida() {
        assertThrows(OrdenException.class, () -> Orden.crear(1L, TipoOperacionOrden.COMPRA, 9L, 101,
                BigDecimal.ONE, BigDecimal.ZERO));

        Orden compra = Orden.crear(1L, TipoOperacionOrden.COMPRA, 9L, 1,
                BigDecimal.ONE, BigDecimal.ZERO);
        compra.cancelar(1L, Instant.now());
        assertEquals(EstadoOrden.CANCELADA, compra.getEstado());
        assertThrows(OrdenException.class,
                () -> compra.ejecutar(1L, 2L, BigDecimal.ONE, Instant.now()));
        assertFalse(compra.compatibleCon(Orden.crear(2L, TipoOperacionOrden.VENTA, 9L, 1,
                BigDecimal.ONE, BigDecimal.ZERO)));
    }
}
