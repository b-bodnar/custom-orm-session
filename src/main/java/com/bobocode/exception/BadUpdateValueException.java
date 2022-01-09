package com.bobocode.exception;

public class BadUpdateValueException extends RuntimeException{
    public BadUpdateValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
