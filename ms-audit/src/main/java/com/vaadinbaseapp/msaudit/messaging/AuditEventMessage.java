package com.vaadinbaseapp.msaudit.messaging;

import java.time.LocalDateTime;

public record AuditEventMessage(String type, String email, String ipAddress, LocalDateTime occurredAt) {
}
