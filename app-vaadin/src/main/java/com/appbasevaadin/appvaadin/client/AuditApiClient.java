package com.appbasevaadin.appvaadin.client;

import com.appbasevaadin.appvaadin.dto.AuditEventResponse;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuditApiClient {

    private final RestClient auditRestClient;

    public AuditApiClient(RestClient auditRestClient) {
        this.auditRestClient = auditRestClient;
    }

    public PageResponse<AuditEventResponse> search(String type, String email, int page, int size) {
        return auditRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/audit-events").queryParam("page", page).queryParam("size", size);
                    if (type != null && !type.isBlank()) {
                        uriBuilder.queryParam("type", type);
                    }
                    if (email != null && !email.isBlank()) {
                        uriBuilder.queryParam("email", email);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(new ParameterizedTypeReference<PageResponse<AuditEventResponse>>() {
                });
    }
}
