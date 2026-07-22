package com.appbasevaadin.msusuarios.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ApiError {

    private final LocalDateTime timestamp;
    private final int status;
    private final String codigo;
    private final String mensaje;
    private final List<ErrorCampo> errores;

    @Getter
    @AllArgsConstructor
    public static class ErrorCampo {
        private final String campo;
        private final String mensaje;
    }
}
