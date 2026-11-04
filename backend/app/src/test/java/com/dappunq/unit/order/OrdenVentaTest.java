package com.dappunq.unit.order;

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

class OrdenVentaTest {
    @Test
    void ventaUsaRangoInferiorInclusivoYNoPermitePrecioNegativo() {
        Orden venta = Orden.crear(2L, TipoOperacionOrden.VENTA, 8L, 4,
                new BigDecimal("12.00"), new BigDecimal("2.00"));
        Orden compra = Orden.crear(1L, TipoOperacionOrden.COMPRA, 8L, 4,
                new BigDecimal("10.00"), BigDecimal.ZERO);

        assertTrue(venta.compatibleCon(compra));
        assertEquals(0, venta.precioMinimo().compareTo(new BigDecimal("10.00")));
        assertEquals(0, Orden.precioDeLiquidacion(venta, compra).compareTo(new BigDecimal("10.00")));

        Orden tolerada = Orden.crear(2L, TipoOperacionOrden.VENTA, 8L, 1,
                BigDecimal.ONE, new BigDecimal("2.00"));
        assertEquals(0, tolerada.precioMinimo().compareTo(BigDecimal.ZERO));
        assertThrows(OrdenException.class, () -> Orden.crear(2L, TipoOperacionOrden.VENTA, 8L, 1,
                new BigDecimal("-1.00"), BigDecimal.ZERO));
    }

    @Test
    void rechazaAutoCoincidenciaYCancelacionDeOtraPersona() {
        Orden venta = Orden.crear(2L, TipoOperacionOrden.VENTA, 8L, 1,
                BigDecimal.ONE, BigDecimal.ZERO);
        Orden propia = Orden.crear(2L, TipoOperacionOrden.COMPRA, 8L, 1,
                BigDecimal.ONE, BigDecimal.ZERO);
        assertFalse(venta.compatibleCon(propia));
        assertThrows(OrdenException.class, () -> venta.cancelar(1L, Instant.now()));
    }

    @Test
    void rangosSinInterseccionNoSeEjecutan() {
        Orden compra = Orden.crear(1L, TipoOperacionOrden.COMPRA, 8L, 1,
                new BigDecimal("10.00"), new BigDecimal("1.00"));
        Orden venta = Orden.crear(2L, TipoOperacionOrden.VENTA, 8L, 1,
                new BigDecimal("20.00"), new BigDecimal("1.00"));

        assertFalse(compra.compatibleCon(venta));
    }
}
