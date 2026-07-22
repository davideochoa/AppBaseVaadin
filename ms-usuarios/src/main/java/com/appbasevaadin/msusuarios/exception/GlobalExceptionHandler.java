package com.appbasevaadin.msusuarios.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidacion(MethodArgumentNotValidException ex) {
        List<ApiError.ErrorCampo> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.ErrorCampo(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "VALIDACION", "Los datos enviados no son validos", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({UsuarioNoEncontradoException.class, TipoUsuarioNoEncontradoException.class})
    public ResponseEntity<ApiError> manejarNoEncontrado(RuntimeException ex) {
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(),
                "NO_ENCONTRADO", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violacion de integridad de datos", ex);
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.CONFLICT.value(),
                "CONFLICTO_DATOS", "El recurso ya existe o viola una restriccion de datos", List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex) {
        log.error("Error no controlado", ex);
        ApiError error = new ApiError(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "ERROR_INTERNO", "Ocurrio un error inesperado", List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
