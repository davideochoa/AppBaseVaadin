package com.appbasevaadin.msaudit.dto;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuditEventResponse {

    private final Long id;
    private final String type;
    private final String email;
    private final String ipAddress;
    private final LocalDateTime occurredAt;
    private final LocalDateTime receivedAt;

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getType(),
                event.getEmail(),
                event.getIpAddress(),
                event.getOccurredAt(),
                event.getReceivedAt()
        );
    }
}
