package com.appbasevaadin.appvaadin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(LocalDateTime timestamp, int status, String code, String message, List<FieldError> errors) {

    public record FieldError(String field, String message) {
    }
}
