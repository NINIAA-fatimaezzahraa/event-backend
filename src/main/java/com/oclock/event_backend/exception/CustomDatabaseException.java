package com.oclock.event_backend.exception;

public class CustomDatabaseException extends RuntimeException {
    public CustomDatabaseException(String message) {
        super(message);
    }
}
