package com.dappunq.exception;

public class DuplicatePlayerException extends RuntimeException {
    public DuplicatePlayerException(String id) {
        super("El jugador ya existe: " + id);
    }
}
