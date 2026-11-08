package com.mission;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler({DateTimeParseException.class, HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String,String>> badRequest(Exception ignored) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error","Invalid request or date. Use YYYY-MM-DD and check required fields."));
    }
}
