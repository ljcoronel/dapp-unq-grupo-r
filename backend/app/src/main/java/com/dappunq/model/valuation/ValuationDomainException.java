package com.dappunq.model.valuation;

public class ValuationDomainException extends RuntimeException {

    public ValuationDomainException(String message) {
        super(message);
    }

    public static ValuationDomainException invalidWeights(String m) {
        return new ValuationDomainException("Pesos invalidos: " + m);
    }

    public static ValuationDomainException pendingWithoutCurrent() {
        return new ValuationDomainException("No existe estrategia actual para crear una pendiente");
    }

    public static ValuationDomainException invalidWindow() {
        return new ValuationDomainException("Ventana de valuacion invalida");
    }

    public static ValuationDomainException snapshotFailure(String m) {
        return new ValuationDomainException("Fallo de snapshot: " + m);
    }
}
