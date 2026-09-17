package com.dappunq.dto;

import com.dappunq.model.Jugador;

public record JugadorResponseDTO(
        String id,
        String nombre,
        String liga,
        String equipo,
        String posicion,
        Integer pases,
        Integer tiros,
        Integer intercepciones,
        Integer calificaciones,
        Integer minutosJugados,
        Integer goles,
        Integer asistencias,
        Integer tirosAlArco,
        Integer pasesRealizados,
        Integer tarjetasAmarillas,
        Integer tarjetasRojas,
        Integer ratingGeneral
) {
    public static JugadorResponseDTO fromEntity(Jugador jugador) {
        return new JugadorResponseDTO(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getLiga(),
                jugador.getEquipo(),
                jugador.getPosicion(),
                jugador.getPases(),
                jugador.getTiros(),
                jugador.getIntercepciones(),
                jugador.getCalificaciones(),
                jugador.getMinutosJugados(),
                jugador.getGoles(),
                jugador.getAsistencias(),
                jugador.getTirosAlArco(),
                jugador.getPasesRealizados(),
                jugador.getTarjetasAmarillas(),
                jugador.getTarjetasRojas(),
                jugador.getRatingGeneral()
        );
    }
}
