package com.dappunq.unit;

import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingJugador;
import com.dappunq.model.ranking.RankingPagina;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RankingDomainTest {
    @Test
    void normalizaFiltrosYValidaPaginacion() {
        var filtros = new RankingFiltros(" Premier League ", " Arsenal ", " Forward ", 2, 20);
        assertEquals("Premier League", filtros.liga());
        assertEquals("Arsenal", filtros.equipo());
        assertEquals("Forward", filtros.posicion());
        assertThrows(IllegalArgumentException.class, () -> new RankingFiltros(null, null, null, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> new RankingFiltros(null, null, null, 1, 101));
    }

    @Test
    void rechazaFiltrosConControlYJugadorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new RankingFiltros("Liga\nInterna", null, null, 1, 20));
        assertThrows(IllegalArgumentException.class, () -> new RankingJugador(
            0, 1L, "Jugador", "Equipo", "Liga", "Forward", BigDecimal.ONE, "ESTRATEGIA_A"));
        assertThrows(IllegalArgumentException.class, () -> new RankingJugador(
            1, 1L, "Jugador", "Equipo", "Liga", "Forward", BigDecimal.valueOf(-1), "ESTRATEGIA_A"));
    }

    @Test
    void paginaConservaMetadatosYItems() {
        var jugador = new RankingJugador(
            1, 7L, "Jugador", "Equipo", "Liga", "Forward", BigDecimal.valueOf(12.5), "ESTRATEGIA_A");
        var pagina = new RankingPagina(List.of(jugador), 1, 1, 20);
        assertEquals(1, pagina.items().size());
        assertEquals(1, pagina.total());
        assertEquals(20, pagina.limite());
    }
}
