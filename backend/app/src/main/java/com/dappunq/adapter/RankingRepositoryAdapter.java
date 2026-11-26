package com.dappunq.adapter;

import com.dappunq.model.ranking.RankingFiltros;
import com.dappunq.model.ranking.RankingJugador;
import com.dappunq.model.ranking.RankingPagina;
import com.dappunq.repository.RankingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RankingRepositoryAdapter implements RankingRepository {
    private final JdbcTemplate jdbcTemplate;

    public RankingRepositoryAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RankingPagina buscar(RankingFiltros filtros) {
        StringBuilder condiciones = new StringBuilder(" WHERE l.external_id IN (2, 3, 4, 5, 22)");
        List<Object> parametros = new ArrayList<>();
        agregarFiltro(condiciones, parametros, "l.name", filtros.liga());
        agregarFiltro(condiciones, parametros, "t.name", filtros.equipo());
        agregarFiltro(condiciones, parametros, "p.position", filtros.posicion());

        long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM players p " +
                "JOIN teams t ON t.id = p.team_id " +
                "JOIN leagues l ON l.id = p.league_id" + condiciones,
            Long.class,
            parametros.toArray()
        );

        String sql = """
            WITH jugadores AS (
                SELECT p.id, p.name, t.name AS equipo, l.name AS liga, p.position,
                       p.external_id
                FROM players p
                JOIN teams t ON t.id = p.team_id
                JOIN leagues l ON l.id = p.league_id
            """ + condiciones + """
            ),
            vigentes AS (
                SELECT v.jugador_id,
                       MAX(v.valor_unitario) FILTER (WHERE v.estrategia = 'ESTRATEGIA_A') AS valor_a,
                       MAX(v.valor_unitario) FILTER (WHERE v.estrategia = 'ESTRATEGIA_B') AS valor_b
                FROM valuaciones v
                JOIN configuraciones_estrategia c
                  ON c.usuario_id = v.usuario_id
                 AND c.tipoestrategia = v.estrategia
                 AND c.estado = 'ACTUAL'
                WHERE v.is_current = true
                  AND v.estrategia IN ('ESTRATEGIA_A', 'ESTRATEGIA_B')
                GROUP BY v.jugador_id
            ),
            bases AS (
                SELECT h.jugador_id, MAX(h.valor_unitario) AS base
                FROM tenencias h
                JOIN usuarios u ON u.id = h.usuario_id
                WHERE u.nombre = 'superuser' AND h.valor_unitario > 0
                GROUP BY h.jugador_id
            ),
            tenencias AS (
                SELECT h.jugador_id, SUM(h.tokens) AS tenencias
                FROM tenencias h
                JOIN usuarios u ON u.id = h.usuario_id
                WHERE u.nombre <> 'superuser'
                GROUP BY h.jugador_id
            ),
            consolidados AS (
                SELECT j.*,
                       CASE
                           WHEN vg.valor_a IS NOT NULL AND vg.valor_b IS NOT NULL
                                AND vg.valor_a >= vg.valor_b THEN vg.valor_a
                           WHEN vg.valor_a IS NOT NULL THEN vg.valor_a
                           WHEN vg.valor_b IS NOT NULL THEN vg.valor_b
                           ELSE COALESCE(b.base, 1.00)
                       END AS valor,
                       CASE
                           WHEN vg.valor_a IS NOT NULL AND vg.valor_b IS NOT NULL
                                AND vg.valor_a >= vg.valor_b THEN 'ESTRATEGIA_A'
                           WHEN vg.valor_a IS NOT NULL AND vg.valor_b IS NULL THEN 'ESTRATEGIA_A'
                           WHEN vg.valor_b IS NOT NULL THEN 'ESTRATEGIA_B'
                           ELSE 'Cotizacion base'
                       END AS origen,
                       COALESCE(b.base, 1.00) AS base,
                       COALESCE(t.tenencias, 0) AS tenencias
                FROM jugadores j
                LEFT JOIN vigentes vg ON vg.jugador_id = j.id
                LEFT JOIN bases b ON b.jugador_id = j.id
                LEFT JOIN tenencias t ON t.jugador_id = j.id
            ),
            ordenados AS (
                SELECT c.*,
                       ROW_NUMBER() OVER (
                           ORDER BY valor DESC, base DESC, tenencias DESC,
                                    name ASC, id ASC
                       ) AS posicion
                FROM consolidados c
            )
            SELECT posicion, id, name, equipo, liga, position, valor, origen
            FROM ordenados
            WHERE posicion BETWEEN ? AND ?
            ORDER BY posicion
            """;

        List<Object> parametrosConsulta = new ArrayList<>(parametros);
        int inicio = (filtros.pagina() - 1) * filtros.limite() + 1;
        int fin = inicio + filtros.limite() - 1;
        parametrosConsulta.add(inicio);
        parametrosConsulta.add(fin);

        List<RankingJugador> items = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new RankingJugador(
                rs.getInt("posicion"),
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("equipo"),
                rs.getString("liga"),
                rs.getString("position"),
                rs.getBigDecimal("valor"),
                rs.getString("origen")
            ),
            parametrosConsulta.toArray()
        );
        return new RankingPagina(items, total, filtros.pagina(), filtros.limite());
    }

    private void agregarFiltro(StringBuilder condiciones, List<Object> parametros,
                               String columna, String valor) {
        if (valor != null) {
            condiciones.append(" AND LOWER(").append(columna).append(") LIKE LOWER(?)");
            parametros.add("%" + valor + "%");
        }
    }
}
