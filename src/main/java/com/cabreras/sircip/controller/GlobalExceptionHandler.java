package com.cabreras.sircip.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Errores de Jakarta Validation (@Pattern, @Min, @Max, etc.)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String paramName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(paramName, violation.getMessage());
        });
        return buildErrorResponse("Error de validación", errors);
    }

    // Errores de tipos de Spring (@DateTimeFormat, letras en campos numéricos, etc.)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errors = new HashMap<>();
        String paramName = ex.getName();
        String errorMessage = "El formato del parámetro es inválido.";
        if ("fecha".equals(paramName)) {
            errorMessage = "Debe utilizar el formato ISO (AAAA-MM-DD).";
        } else if ("jurisdiccion".equals(paramName)) {
            errorMessage = "Debe ser un número válido.";
        }
        errors.put(paramName, errorMessage);
        return buildErrorResponse("Error en el tipo de datos", errors);
    }

    private Map<String, Object> buildErrorResponse(String errorType, Map<String, String> details) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", errorType);
        response.put("details", details);
        return response;
    }
}
