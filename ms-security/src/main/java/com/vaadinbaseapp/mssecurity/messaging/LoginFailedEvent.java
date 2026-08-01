package com.vaadinbaseapp.mssecurity.messaging;

import java.time.LocalDateTime;

public record LoginFailedEvent(String type, String email, String ipAddress, LocalDateTime occurredAt) {

    public static LoginFailedEvent now(String email, String ipAddress) {
        return new LoginFailedEvent("LOGIN_FAILED", email, ipAddress, LocalDateTime.now());
    }
}
