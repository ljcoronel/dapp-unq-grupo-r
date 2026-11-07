package com.dappunq.unit.order;

import com.dappunq.model.order.SaldoCreditos;
import com.dappunq.model.order.exception.OrdenException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaldoCreditosTest {
    @Test
    void reservaLiquidaYLiberaElExcedenteSinPerderCreditos() {
        SaldoCreditos saldo = new SaldoCreditos(new BigDecimal("100.00"), BigDecimal.ZERO);
        saldo.reservar(new BigDecimal("22.00"));
        saldo.liquidarCompra(new BigDecimal("22.00"), new BigDecimal("20.50"));

        assertEquals(0, saldo.getDisponibles().compareTo(new BigDecimal("79.50")));
        assertEquals(0, saldo.getBloqueados().compareTo(BigDecimal.ZERO));
        saldo.acreditar(new BigDecimal("3.00"));
        assertEquals(0, saldo.getDisponibles().compareTo(new BigDecimal("82.50")));
    }

    @Test
    void rechazaReservasInsuficientesYPrecisionExcesiva() {
        SaldoCreditos saldo = new SaldoCreditos(new BigDecimal("5.00"), BigDecimal.ZERO);

        assertThrows(OrdenException.class, () -> saldo.reservar(new BigDecimal("5.01")));
        assertThrows(OrdenException.class,
                () -> new SaldoCreditos(new BigDecimal("1.001"), BigDecimal.ZERO));
        assertEquals(0, saldo.getDisponibles().compareTo(new BigDecimal("5.00")));
    }
}
