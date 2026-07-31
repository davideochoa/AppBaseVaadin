package com.appbasevaadin.appvaadin.dto;

public record TokenResponse(String accessToken, String refreshToken, boolean mustResetPassword, String tokenType) {
}
