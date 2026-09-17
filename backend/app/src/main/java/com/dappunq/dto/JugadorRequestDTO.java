package com.dappunq.dto;

public record JugadorRequestDTO(
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
    public JugadorRequestDTO {
        id = sanitize(id);
        nombre = sanitize(nombre);
        liga = sanitize(liga);
        equipo = sanitize(equipo);
        posicion = sanitize(posicion);
        pases = valueOrZero(pases);
        tiros = valueOrZero(tiros);
        intercepciones = valueOrZero(intercepciones);
        calificaciones = valueOrZero(calificaciones);
        minutosJugados = valueOrZero(minutosJugados);
        goles = valueOrZero(goles);
        asistencias = valueOrZero(asistencias);
        tirosAlArco = valueOrZero(tirosAlArco);
        pasesRealizados = valueOrZero(pasesRealizados);
        tarjetasAmarillas = valueOrZero(tarjetasAmarillas);
        tarjetasRojas = valueOrZero(tarjetasRojas);
        ratingGeneral = valueOrZero(ratingGeneral);
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    private static Integer valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
