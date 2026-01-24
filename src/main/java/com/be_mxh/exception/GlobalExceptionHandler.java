package com.be_mxh.exception;


import com.be_mxh.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Login sai / Authentication lỗi (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<String>builder()
                        .code(401)
                        .message("Invalid username or password")
                        .data("UNAUTHORIZED")
                        .build()
        );
    }

    // Validation lỗi (@Valid) (400) @NotNull @NotBlank @Past @Pattern
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(
                ApiResponse.<Map>builder()
                        .code(400)
                        .message("Invalid fields")
                        .data(errors)
                        .build());
    }

    // Validation lỗi (@Valid) (400) @Other annotation binding
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {

            String field = error.getField();
            String message = "Invalid value";

            Throwable cause = error.unwrap(Throwable.class);

            if (cause instanceof DateTimeParseException) {
                message = "Invalid date format. Expected yyyy-MM-dd";
            }
            else if (cause instanceof IllegalArgumentException) {
                message = "Invalid value";
            }

            errors.put(field, message);
        });

        return ResponseEntity.badRequest().body(
                ApiResponse.<Map>builder()
                        .code(400)
                        .message("Invalid fields")
                        .data(errors)
                        .build());
    }

    // Runtime exception chung (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<Object>builder()
                        .code(500)
                        .message("INTERNAL_SERVER_ERROR")
                        .data(new Object())
                        .build()
        );
    }
}
