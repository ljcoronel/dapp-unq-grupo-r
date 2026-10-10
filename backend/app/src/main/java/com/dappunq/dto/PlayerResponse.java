package com.dappunq.dto;

public record PlayerResponse(
        Integer id,
        String nombre,
        String seccion,
        String equipo,
        LeagueResponse liga,
        Integer partidosJugados,
        Integer goles,
        Integer asistencias,
        Integer penaltis
) {
}
