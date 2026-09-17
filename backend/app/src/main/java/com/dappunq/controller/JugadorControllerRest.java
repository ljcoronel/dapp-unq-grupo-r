package com.dappunq.controller;

import com.dappunq.dto.JugadorResponseDTO;
import com.dappunq.service.JugadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class JugadorControllerRest {
    private final JugadorService jugadorService;

    public JugadorControllerRest(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @GetMapping
    public ResponseEntity<List<JugadorResponseDTO>> listPlayers(@RequestParam(required = false) String liga,
                                                              @RequestParam(required = false) String equipo,
                                                              @RequestParam(required = false) String posicion) {
        return ResponseEntity.ok(jugadorService.findAll(liga, equipo, posicion));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JugadorResponseDTO> getPlayer(@PathVariable String id) {
        return ResponseEntity.ok(jugadorService.findById(id));
    }
}
