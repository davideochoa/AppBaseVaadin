package com.appbasevaadin.msaudit.controller;

import com.appbasevaadin.msaudit.dto.AuditEventResponse;
import com.appbasevaadin.msaudit.service.AuditEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit-events")
public class AuditEventController {

    private final AuditEventService auditEventService;

    public AuditEventController(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public Page<AuditEventResponse> search(@RequestParam(required = false) String type,
                                            @RequestParam(required = false) String email,
                                            Pageable pageable) {
        return auditEventService.search(type, email, pageable)
                .map(AuditEventResponse::from);
    }
}
