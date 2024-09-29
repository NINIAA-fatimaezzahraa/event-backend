package com.oclock.event_backend.exception;

import com.oclock.event_backend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("NOT_FOUND")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("BAD_REQUEST")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmptyInputException.class)
    public ResponseEntity<ErrorResponse> handleEmptyInputException(EmptyInputException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("EMPTY_INPUT")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateFormatException.class)
    public ResponseEntity<ErrorResponse> handleDateFormatException(DateFormatException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("INVALID_DATE_FORMAT")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("INVALID_CREDENTIALS")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CustomDatabaseException.class)
    public ResponseEntity<ErrorResponse> handleCustomDatabaseException(CustomDatabaseException ex) {
        logger.error("Database error occurred: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("DATABASE_ERROR")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ErrorResponse> handleFunctionalException(FunctionalException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("FUNCTIONAL_ERROR")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("PERMISSION_ERROR")
                .message("Access Denied: You don't have permission to access this resource. " +ex)
                .build();

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = ErrorResponse
                .builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
