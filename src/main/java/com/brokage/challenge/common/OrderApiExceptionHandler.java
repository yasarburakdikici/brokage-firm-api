package com.brokage.challenge.common;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.brokage.challenge.exception.InvalidCustomerException;
import com.brokage.challenge.exception.InvalidOrderException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.brokage.challenge.exception.BrokageFirmApiException;

@RestControllerAdvice
public class OrderApiExceptionHandler {

    @ExceptionHandler(BrokageFirmApiException.class)
    public ResponseEntity<OrderApiErrorResponse> handleGenericError(BrokageFirmApiException exception) {
        return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new OrderApiErrorResponse("Internal Server Error", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OrderApiErrorResponse> handleNotValidMethodArgumentError(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new OrderApiErrorResponse(
                    "Validation failed",
                    errors
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<OrderApiErrorResponse> handleNotReadableError(HttpMessageNotReadableException exception) {

        String specificErrorMessage = "Invalid data in request body.";

        Throwable rootCause = exception.getCause();
        if (rootCause instanceof InvalidFormatException invalidFormatException) {

            String fieldName = invalidFormatException.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .findFirst().orElse("Unknown Field");
            specificErrorMessage = String.format("Invalid value for '%s'.", fieldName);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new OrderApiErrorResponse(
                        "Request Body Conversion Error (JSON/Data)",
                        Collections.singletonList(specificErrorMessage)
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OrderApiErrorResponse> handleIllegalArgumentError(IllegalArgumentException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new OrderApiErrorResponse(exception.getMessage(), null));
    }

    @ExceptionHandler(InvalidCustomerException.class)
    public ResponseEntity<OrderApiErrorResponse> handleInvalidCustomerError(InvalidCustomerException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new OrderApiErrorResponse(exception.getMessage(), null));
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<OrderApiErrorResponse> handleInvalidOrderError(InvalidOrderException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new OrderApiErrorResponse(exception.getMessage(), null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<OrderApiErrorResponse> handleMissingServletRequestParameterError(MissingServletRequestParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new OrderApiErrorResponse(exception.getMessage(), null));
    }




    
}


