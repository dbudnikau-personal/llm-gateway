package com.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<?> handleBackendError(RestClientException ex) {
        log.error("LLM backend request failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("type", "error", "error", Map.of(
                        "type", "backend_error",
                        "message", "upstream LLM backend returned an error")));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleNoBackend(IllegalStateException ex) {
        log.error("No LLM backend available: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("type", "error", "error", Map.of(
                        "type", "service_unavailable",
                        "message", "no LLM backend available to handle the request")));
    }
}
