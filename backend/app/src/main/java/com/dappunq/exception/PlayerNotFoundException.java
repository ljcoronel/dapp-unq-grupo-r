package com.dappunq.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(Integer id) {
        super("No existe un jugador con el identificador " + id);
    }
}
