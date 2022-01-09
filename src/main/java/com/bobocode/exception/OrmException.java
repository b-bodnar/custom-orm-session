package com.bobocode.exception;

public class OrmException extends RuntimeException{

    public OrmException(String message, Throwable cause) {
        super(message, cause);
    }
}
