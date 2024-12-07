package com.jwt.exception;

public class InvalidTokenExceptionHandler extends RuntimeException {
    public InvalidTokenExceptionHandler(String message) {
        super(message);
    }
}
