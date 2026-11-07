package com.dappunq.controller;

import com.dappunq.model.order.exception.OrdenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class OrderExceptionHandler {

    @ExceptionHandler(OrdenException.class)
    public ResponseEntity<Map<String, String>> handle(OrdenException exception) {
        HttpStatus status = switch (exception.getCodigo()) {
            case ORDEN_NO_ENCONTRADA, JUGADOR_NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case PROPIETARIO_INVALIDO -> HttpStatus.FORBIDDEN;
            case ESTADO_ORDEN_INVALIDO -> HttpStatus.CONFLICT;
            case RECURSOS_INSUFICIENTES, ESTRATEGIA_INACTIVA, ORDEN_INVALIDA -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
    }
}
