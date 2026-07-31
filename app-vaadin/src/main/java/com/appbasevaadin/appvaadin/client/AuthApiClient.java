package com.appbasevaadin.appvaadin.client;

import com.appbasevaadin.appvaadin.dto.ApiError;
import com.appbasevaadin.appvaadin.dto.GoogleLoginRequest;
import com.appbasevaadin.appvaadin.dto.LoginRequest;
import com.appbasevaadin.appvaadin.dto.RefreshRequest;
import com.appbasevaadin.appvaadin.dto.TokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthApiClient {

    private final RestClient securityRestClient;

    public AuthApiClient(RestClient securityRestClient) {
        this.securityRestClient = securityRestClient;
    }

    public TokenResponse login(String username, String password) {
        return securityRestClient.post()
                .uri("/login")
                .body(new LoginRequest(username, password))
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(TokenResponse.class);
    }

    public TokenResponse loginWithGoogle(String idToken) {
        return securityRestClient.post()
                .uri("/login/google")
                .body(new GoogleLoginRequest(idToken))
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(TokenResponse.class);
    }

    public TokenResponse refresh(String refreshToken) {
        return securityRestClient.post()
                .uri("/refresh")
                .body(new RefreshRequest(refreshToken))
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(TokenResponse.class);
    }

    public void logout(String refreshToken) {
        securityRestClient.post()
                .uri("/logout")
                .body(new RefreshRequest(refreshToken))
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .toBodilessEntity();
    }
}
