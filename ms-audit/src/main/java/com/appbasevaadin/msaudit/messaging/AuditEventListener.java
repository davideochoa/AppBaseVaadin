package com.appbasevaadin.msaudit.messaging;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import com.appbasevaadin.msaudit.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditEventRepository auditEventRepository;

    public AuditEventListener(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.audit-topic}", groupId = "ms-audit")
    public void onMessage(AuditEventMessage message) {
        log.info("Received audit event: type={}, email={}", message.type(), message.email());

        AuditEvent event = new AuditEvent();
        event.setType(message.type());
        event.setEmail(message.email());
        event.setIpAddress(message.ipAddress());
        event.setOccurredAt(message.occurredAt());
        auditEventRepository.save(event);
    }
}
