package com.appbasevaadin.appvaadin.client;

import com.appbasevaadin.appvaadin.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

final class ApiClientSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private ApiClientSupport() {
    }

    static void handleError(HttpRequest request, ClientHttpResponse response) throws IOException {
        ApiError apiError;
        try (InputStream body = response.getBody()) {
            apiError = OBJECT_MAPPER.readValue(body, ApiError.class);
        } catch (Exception e) {
            apiError = new ApiError(null, response.getStatusCode().value(), "UNKNOWN",
                    "Request failed with status " + response.getStatusCode().value(), List.of());
        }
        throw new ApiException(apiError);
    }
}
