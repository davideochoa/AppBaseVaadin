package com.vaadinbaseapp.msaudit.messaging;

import com.vaadinbaseapp.msaudit.entity.AuditEvent;
import com.vaadinbaseapp.msaudit.mapper.AuditEventMapper;
import com.vaadinbaseapp.msaudit.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditEventRepository auditEventRepository;
    private final AuditEventMapper auditEventMapper;

    public AuditEventListener(AuditEventRepository auditEventRepository, AuditEventMapper auditEventMapper) {
        this.auditEventRepository = auditEventRepository;
        this.auditEventMapper = auditEventMapper;
    }

    @KafkaListener(topics = "${app.kafka.audit-topic}", groupId = "ms-audit")
    public void onMessage(AuditEventMessage message) {
        log.info("Received audit event: type={}, email={}", message.type(), message.email());

        AuditEvent event = auditEventMapper.toEntity(message);
        auditEventRepository.save(event);
    }
}
