package com.example.helloworld.controller;

import com.example.helloworld.dto.ErrorResponse;
import com.example.helloworld.exception.InvalidInputException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Returns 400 with {"error": "Invalid Input"} for bad requests
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final ErrorResponse INVALID_INPUT = new ErrorResponse("Invalid Input");

    // Always return JSON
    @ExceptionHandler({InvalidInputException.class, ServletRequestBindingException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInput(Exception ex, HttpServletRequest request) {
        // Log the reason, but not the name
        log.warn("Rejected {} {}: {}", request.getMethod(), request.getRequestURI(),
                LogFormatUtils.formatValue(ex.getMessage(), -1, true));
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(INVALID_INPUT);
    }
}
