package com.dappunq.model.valuation;

public class ValuationDomainException extends RuntimeException {

    public ValuationDomainException(String message) {
        super(message);
    }

    public static ValuationDomainException invalidWeights(String m) {
        return new ValuationDomainException("Pesos invalidos: " + m);
    }

    public static ValuationDomainException snapshotFailure(String m) {
        return new ValuationDomainException("Fallo de snapshot: " + m);
    }
}
