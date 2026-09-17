package com.dappunq.exception;

public class JugadorNotFoundException extends RuntimeException {
    public JugadorNotFoundException(String id) {
        super("Jugador no encontrado: " + id);
    }
}
