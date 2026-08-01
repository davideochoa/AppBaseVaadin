package com.vaadinbaseapp.appvaadin.dto;

public record TokenResponse(String accessToken, String refreshToken, String tokenType) {
}
