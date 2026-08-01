package com.vaadinbaseapp.mssecurity.config;

import com.vaadinbaseapp.mssecurity.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Single place that knows how to serialize a filter-level rejection (401/403/429, raised before
 * the request ever reaches a controller) into the same {@link ApiError} JSON shape the rest of
 * the API uses. {@link JwtAuthEntryPoint}, {@link JwtAccessDeniedHandler} and
 * {@link RateLimitFilter} only decide *which* status/code/message applies to their event and
 * delegate the serialization here.
 */
@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), code, message, List.of());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
