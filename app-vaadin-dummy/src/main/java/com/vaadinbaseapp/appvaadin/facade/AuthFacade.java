package com.vaadinbaseapp.appvaadin.facade;

import com.vaadinbaseapp.appvaadin.auth.AuthenticatedUser;
import com.vaadinbaseapp.appvaadin.client.ApiException;
import com.vaadinbaseapp.appvaadin.dto.ApiError;
import com.vaadinbaseapp.appvaadin.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stands in for the real AuthApiClient + ms-security backend. Accepts a couple of fixed demo
 * accounts and mints a self-contained, unsigned "JWT" so {@link AuthenticatedUser}'s local claim
 * decoding keeps working unchanged - there is no signature to actually verify here. This module
 * has no real backend and is not part of any deployment (no Dockerfile, not in docker-compose.yml
 * or CI); if that ever changes, this class needs a real ms-security-backed implementation instead.
 */
@Component
public class AuthFacade {

    private static final Logger log = LoggerFactory.getLogger(AuthFacade.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AuthenticatedUser authenticatedUser;
    private final List<DemoAccount> demoAccounts;

    public AuthFacade(AuthenticatedUser authenticatedUser,
                       @Value("${app.demo.admin-email:admin@local}") String adminEmail,
                       @Value("${app.demo.admin-password:admin123}") String adminPassword,
                       @Value("${app.demo.user-email:user@local}") String userEmail,
                       @Value("${app.demo.user-password:user123}") String userPassword) {
        this.authenticatedUser = authenticatedUser;
        this.demoAccounts = List.of(
                new DemoAccount(adminEmail, adminPassword, "ADMINISTRATOR"),
                new DemoAccount(userEmail, userPassword, "USER"));
        log.warn("app-vaadin-dummy is running with mock, offline authentication (fixed demo "
                + "accounts, unsigned tokens, no real ms-security backend). Never point this "
                + "module at a shared or production environment.");
    }

    public void login(String email, String password) {
        DemoAccount account = demoAccounts.stream()
                .filter(a -> a.email().equalsIgnoreCase(email) && a.password().equals(password))
                .findFirst()
                .orElseThrow(() -> new ApiException(new ApiError(LocalDateTime.now(), 401, "AUTHENTICATION_FAILED",
                        "Invalid email or password", List.of())));
        authenticatedUser.login(issueTokens(account.email(), account.role()));
    }

    public void loginWithGoogle(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new ApiException(new ApiError(LocalDateTime.now(), 401, "AUTHENTICATION_FAILED",
                    "Missing Google credential", List.of()));
        }
        authenticatedUser.login(issueTokens("google.demo@example.com", "USER"));
    }

    public void logout() {
        authenticatedUser.logout();
    }

    private TokenResponse issueTokens(String email, String role) {
        String accessToken = buildFakeJwt(email, role, Duration.ofMinutes(10));
        String refreshToken = UUID.randomUUID().toString();
        return new TokenResponse(accessToken, refreshToken, "Bearer");
    }

    private String buildFakeJwt(String email, String role, Duration validity) {
        try {
            String header = encode(OBJECT_MAPPER.writeValueAsBytes(Map.of("alg", "none", "typ", "JWT")));
            Map<String, Object> claims = Map.of(
                    "sub", email,
                    "email", email,
                    "role", role,
                    "exp", Instant.now().plus(validity).getEpochSecond());
            String payload = encode(OBJECT_MAPPER.writeValueAsBytes(claims));
            return header + "." + payload + ".dummy-signature";
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build dummy token", e);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record DemoAccount(String email, String password, String role) {
    }
}
