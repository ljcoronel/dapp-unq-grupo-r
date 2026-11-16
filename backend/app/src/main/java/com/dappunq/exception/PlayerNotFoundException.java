package com.dappunq.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Long id) {
        super("No existe un jugador con el identificador " + id);
    }
}
