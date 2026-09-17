package com.dappunq.service;

import com.dappunq.dto.JugadorResponseDTO;
import com.dappunq.exception.JugadorNotFoundException;
import com.dappunq.model.Jugador;
import com.dappunq.persistence.JugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class JugadorService {
    private final JugadorRepository jugadorRepository;

    public JugadorService(JugadorRepository jugadorRepository) {
        this.jugadorRepository = jugadorRepository;
    }

    @Transactional(readOnly = true)
    public List<JugadorResponseDTO> findAll(String liga, String equipo, String posicion) {
        List<Jugador> jugadores = filterPlayers(liga, equipo, posicion);
        List<JugadorResponseDTO> response = new ArrayList<>();
        for (Jugador jugador : jugadores) {
            response.add(JugadorResponseDTO.fromEntity(jugador));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public JugadorResponseDTO findById(String id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new JugadorNotFoundException(id));
        return JugadorResponseDTO.fromEntity(jugador);
    }

    private List<Jugador> filterPlayers(String liga, String equipo, String posicion) {
        if (hasText(liga) && hasText(equipo) && hasText(posicion)) {
            return jugadorRepository.findByLigaIgnoreCaseAndEquipoIgnoreCaseAndPosicionIgnoreCase(liga.trim(), equipo.trim(), posicion.trim());
        }
        if (hasText(liga) && hasText(equipo)) {
            return jugadorRepository.findByLigaIgnoreCaseAndEquipoIgnoreCase(liga.trim(), equipo.trim());
        }
        if (hasText(liga) && hasText(posicion)) {
            return jugadorRepository.findByLigaIgnoreCaseAndPosicionIgnoreCase(liga.trim(), posicion.trim());
        }
        if (hasText(equipo) && hasText(posicion)) {
            return jugadorRepository.findByEquipoIgnoreCaseAndPosicionIgnoreCase(equipo.trim(), posicion.trim());
        }
        if (hasText(liga)) {
            return jugadorRepository.findByLigaIgnoreCase(liga.trim());
        }
        if (hasText(equipo)) {
            return jugadorRepository.findByEquipoIgnoreCase(equipo.trim());
        }
        if (hasText(posicion)) {
            return jugadorRepository.findByPosicionIgnoreCase(posicion.trim());
        }
        return jugadorRepository.findAll();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
