package com.dappunq.persistence;

import com.dappunq.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, String> {
    List<Jugador> findByLigaIgnoreCase(String liga);
    List<Jugador> findByEquipoIgnoreCase(String equipo);
    List<Jugador> findByPosicionIgnoreCase(String posicion);
    List<Jugador> findByLigaIgnoreCaseAndEquipoIgnoreCase(String liga, String equipo);
    List<Jugador> findByLigaIgnoreCaseAndPosicionIgnoreCase(String liga, String posicion);
    List<Jugador> findByEquipoIgnoreCaseAndPosicionIgnoreCase(String equipo, String posicion);
    List<Jugador> findByLigaIgnoreCaseAndEquipoIgnoreCaseAndPosicionIgnoreCase(String liga, String equipo, String posicion);
}
