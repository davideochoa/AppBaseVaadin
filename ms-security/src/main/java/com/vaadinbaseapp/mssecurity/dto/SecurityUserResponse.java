package com.vaadinbaseapp.mssecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SecurityUserResponse {

    private final String username;
    private final String email;
    private final String role;
    private final boolean active;
    private final boolean mustResetPassword;

    /**
     * Only populated by {@code create}/{@code resetPassword} — the one-time,
     * randomly generated temporary password the admin must relay to the user
     * out-of-band, since {@code mustResetPassword} means the user needs a
     * current password to complete the forced-reset login flow. Null for
     * every other response (e.g. {@code update}, where no password changed).
     */
    private final String temporaryPassword;
}
