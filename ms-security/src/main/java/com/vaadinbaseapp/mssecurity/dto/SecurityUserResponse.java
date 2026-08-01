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
}
