package com.dappunq.exception;

public class InvalidUserDataException extends IllegalArgumentException {
    public InvalidUserDataException(String message) {
        super(message);
    }
}
