package com.appbasevaadin.msaudit.service;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import com.appbasevaadin.msaudit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditEventService {

    private final AuditEventRepository auditEventRepository;

    public AuditEventService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public Page<AuditEvent> search(String type, String email, Pageable pageable) {
        return auditEventRepository.search(type, email, pageable);
    }
}
