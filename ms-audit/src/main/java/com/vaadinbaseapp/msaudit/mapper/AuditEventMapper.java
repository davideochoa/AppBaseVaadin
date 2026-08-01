package com.vaadinbaseapp.msaudit.mapper;

import com.vaadinbaseapp.msaudit.dto.AuditEventResponse;
import com.vaadinbaseapp.msaudit.entity.AuditEvent;
import com.vaadinbaseapp.msaudit.messaging.AuditEventMessage;
import org.springframework.stereotype.Component;

/**
 * Single place for {@code AuditEvent} mapping in both directions: incoming Kafka message → entity
 * (used by {@link com.vaadinbaseapp.msaudit.messaging.AuditEventListener}) and entity → response
 * DTO (used by {@link com.vaadinbaseapp.msaudit.controller.AuditEventController}). Keeps that
 * mapping out of both the listener (whose job is consuming messages) and the DTO (a plain data
 * holder).
 */
@Component
public class AuditEventMapper {

    public AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getType(),
                event.getEmail(),
                event.getIpAddress(),
                event.getOccurredAt(),
                event.getReceivedAt()
        );
    }

    public AuditEvent toEntity(AuditEventMessage message) {
        AuditEvent event = new AuditEvent();
        event.setType(message.type());
        event.setEmail(message.email());
        event.setIpAddress(message.ipAddress());
        event.setOccurredAt(message.occurredAt());
        return event;
    }
}
