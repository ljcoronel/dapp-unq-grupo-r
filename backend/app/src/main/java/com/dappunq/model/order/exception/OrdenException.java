package com.dappunq.model.order.exception;

public class OrdenException extends RuntimeException {
    private final CodigoErrorOrden codigo;

    public OrdenException(CodigoErrorOrden codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public CodigoErrorOrden getCodigo() {
        return codigo;
    }
}
