package com.appbasevaadin.msaudit.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "VALIDATION", "The submitted data is invalid", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // AuthorizationDeniedException (thrown by @PreAuthorize) extends AccessDeniedException.
    // Must be handled here explicitly (Lesson 9): DispatcherServlet resolves @ExceptionHandlers
    // before the exception can propagate to Spring Security's ExceptionTranslationFilter, so
    // without this the catch-all handler below would swallow it as a 500.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                "FORBIDDEN", "You do not have permission to access this resource", List.of());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled error", ex);
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR", "An unexpected error occurred", List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
