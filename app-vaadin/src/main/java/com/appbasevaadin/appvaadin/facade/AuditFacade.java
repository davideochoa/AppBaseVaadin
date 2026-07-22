package com.appbasevaadin.appvaadin.facade;

import com.appbasevaadin.appvaadin.client.AuditApiClient;
import com.appbasevaadin.appvaadin.dto.AuditEventResponse;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import org.springframework.stereotype.Component;

@Component
public class AuditFacade {

    private final AuditApiClient auditApiClient;

    public AuditFacade(AuditApiClient auditApiClient) {
        this.auditApiClient = auditApiClient;
    }

    public PageResponse<AuditEventResponse> search(String type, String email, int page, int size) {
        return auditApiClient.search(type, email, page, size);
    }
}
