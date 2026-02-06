package com.be_mxh.exception;


import com.be_mxh.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import tools.jackson.databind.exc.InvalidFormatException;

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
                        .data(null)
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
            } else if (cause instanceof IllegalArgumentException) {
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

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<String>builder()
                        .code(401)
                        .message("Invalid username or password")
                        .data(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.builder()
                        .code(403)
                        .message("Forbidden")
                        .data("Access Denied. You don't have permission to access this resource.")
                        .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getCause();

        // Bắt lỗi sai format date / datetime
        if (cause instanceof InvalidFormatException ife) {

            String fieldName = "";
            if (!ife.getPath().isEmpty()) {
                fieldName = ife.getPath().get(0).getPropertyName();
            }

            String message = "Invalid format";
            Map<String, String> errors = new HashMap<>();
            if ("dateOfBirth".equals(fieldName)) {
                errors.put("dateOfBirth", "dateOfBirth must be in format yyyy-MM-dd");
            }

            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.builder()
                            .code(400)
                            .message(message)
                            .data(errors)
                            .build());
        }

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.builder()
                        .code(400)
                        .message("Malformed JSON request")
                        .data(null)
                        .build());
    }

    // Response cho lỗi 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.builder()
                        .code(404)
                        .message("Not Found")
                        .data("API not found: " + ex.getRequestURL())
                        .build());
    }

    // Runtime exception chung (500)
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
//        ex.printStackTrace();
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                ApiResponse.<String>builder()
//                        .code(500)
//                        .message("Internal server errors")
//                        .data(ex.getMessage())
//                        .build()
//        );
//    }
}
