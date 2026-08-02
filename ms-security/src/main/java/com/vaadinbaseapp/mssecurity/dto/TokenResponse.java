package com.vaadinbaseapp.mssecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private final boolean mustResetPassword;
    private final String resetToken;
    private final String tokenType = "Bearer";
}
