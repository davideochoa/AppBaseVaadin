package com.appbasevaadin.appvaadin.dto;

import java.time.LocalDateTime;

public record AuditEventResponse(Long id, String type, String email, String ipAddress,
                                  LocalDateTime occurredAt, LocalDateTime receivedAt) {
}
