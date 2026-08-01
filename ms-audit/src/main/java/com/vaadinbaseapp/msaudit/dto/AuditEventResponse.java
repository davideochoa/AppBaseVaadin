package com.vaadinbaseapp.msaudit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Plain response payload — mapping from {@code AuditEvent} lives in
 * {@link com.vaadinbaseapp.msaudit.mapper.AuditEventMapper}, not here, so this class stays a pure
 * data holder.
 */
@Getter
@AllArgsConstructor
public class AuditEventResponse {

    private final Long id;
    private final String type;
    private final String email;
    private final String ipAddress;
    private final LocalDateTime occurredAt;
    private final LocalDateTime receivedAt;
}
