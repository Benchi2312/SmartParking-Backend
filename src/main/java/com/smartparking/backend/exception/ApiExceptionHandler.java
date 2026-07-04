package com.smartparking.backend.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud invalida",
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMessage() != null ? ex.getMessage().toUpperCase() : "";

        String error;
        String detail;

        if (message.contains("USUARIO") && (message.contains("EMAIL") || message.contains("UK"))) {
            error = "Correo duplicado";
            detail = "Ya existe un usuario registrado con ese correo electronico";
        } else if (message.contains("VEHICULO") && (message.contains("PLACA") || message.contains("UK"))) {
            error = "Placa duplicada";
            detail = "Ya existe un vehiculo registrado con esa placa";
        } else if (message.contains("ESPACIO") && (message.contains("NUMERO") || message.contains("UK"))) {
            error = "Numero duplicado";
            detail = "Ya existe un espacio registrado con ese numero";
        } else {
            error = "Operacion no permitida";
            detail = "No se puede realizar la operacion debido a restricciones de integridad en la base de datos";
        }

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                error,
                detail
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
