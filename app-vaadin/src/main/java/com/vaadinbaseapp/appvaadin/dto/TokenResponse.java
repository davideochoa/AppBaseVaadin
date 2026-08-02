package com.vaadinbaseapp.appvaadin.dto;

public record TokenResponse(String accessToken, String refreshToken, boolean mustResetPassword, String resetToken,
                             String tokenType) {
}
