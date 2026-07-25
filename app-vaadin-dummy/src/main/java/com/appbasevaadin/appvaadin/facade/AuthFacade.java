package com.appbasevaadin.appvaadin.facade;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.client.ApiException;
import com.appbasevaadin.appvaadin.dto.ApiError;
import com.appbasevaadin.appvaadin.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * decoding keeps working unchanged - there is no signature to actually verify here.
 */
@Component
public class AuthFacade {

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("admin@local", "admin123", "ADMINISTRATOR"),
            new DemoAccount("user@local", "user123", "USER"));

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AuthenticatedUser authenticatedUser;

    public AuthFacade(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public void login(String email, String password) {
        DemoAccount account = DEMO_ACCOUNTS.stream()
                .filter(a -> a.email().equalsIgnoreCase(email) && a.password().equals(password))
                .findFirst()
                .orElseThrow(() -> new ApiException(new ApiError(LocalDateTime.now(), 401, "AUTHENTICATION_FAILED",
                        "Invalid email or password", List.of())));
        authenticatedUser.login(issueTokens(account.email(), account.role()));
    }

    public void loginWithGoogle(String idToken) {
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
