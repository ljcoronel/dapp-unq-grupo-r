package com.dappunq.unit.order;

import com.dappunq.model.order.TenenciaTokens;
import com.dappunq.model.order.exception.OrdenException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenenciaTokensTest {
    @Test
    void reservaLiberaTransfiereYRecibeTokens() {
        TenenciaTokens tokens = new TenenciaTokens(10, 0);
        tokens.reservar(4);
        assertEquals(6, tokens.disponibles());
        tokens.liberar(1);
        tokens.transferirBloqueados(3);
        tokens.recibir(2);
        assertEquals(9, tokens.getCantidad());
        assertEquals(0, tokens.getBloqueados());
    }

    @Test
    void rechazaBloqueosMayoresALaTenenciaYReservasInsuficientes() {
        assertThrows(OrdenException.class, () -> new TenenciaTokens(2, 3));
        TenenciaTokens tokens = new TenenciaTokens(2, 0);
        assertThrows(OrdenException.class, () -> tokens.reservar(3));
        assertEquals(2, tokens.getCantidad());
    }
}
