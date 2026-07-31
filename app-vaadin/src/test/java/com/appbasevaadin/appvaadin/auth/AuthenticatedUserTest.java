package com.appbasevaadin.appvaadin.auth;

import com.appbasevaadin.appvaadin.dto.TokenResponse;
import com.appbasevaadin.appvaadin.testutil.KaribuTestSetup;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mvysny.kaributesting.v10.MockVaadin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticatedUserTest {

    private final AuthenticatedUser authenticatedUser = new AuthenticatedUser();

    @BeforeEach
    void setUp() {
        KaribuTestSetup.setupProductionMode();
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    private String fakeJwt(String email, String role, long expEpochSeconds) {
        try {
            Map<String, Object> payload = Map.of("email", email, "role", role, "exp", expEpochSeconds);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(new ObjectMapper().writeValueAsBytes(payload));
            return "header." + encodedPayload + ".signature";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void loginMakesTheUserAuthenticatedWithDecodedClaims() {
        String token = fakeJwt("jane@example.com", "ADMINISTRATOR", Instant.now().plusSeconds(600).getEpochSecond());
        authenticatedUser.login(new TokenResponse(token, "refresh-token", false, "Bearer"));

        assertThat(authenticatedUser.isAuthenticated()).isTrue();
        assertThat(authenticatedUser.getEmail()).contains("jane@example.com");
        assertThat(authenticatedUser.hasRole("ADMINISTRATOR")).isTrue();
        assertThat(authenticatedUser.hasRole("USER")).isFalse();
    }

    @Test
    void anExpiredTokenIsNotConsideredAuthenticated() {
        String token = fakeJwt("jane@example.com", "USER", Instant.now().minusSeconds(60).getEpochSecond());
        authenticatedUser.login(new TokenResponse(token, "refresh-token", false, "Bearer"));

        assertThat(authenticatedUser.isAuthenticated()).isFalse();
    }

    @Test
    void logoutClearsTheSession() {
        String token = fakeJwt("jane@example.com", "USER", Instant.now().plusSeconds(600).getEpochSecond());
        authenticatedUser.login(new TokenResponse(token, "refresh-token", false, "Bearer"));

        authenticatedUser.logout();

        assertThat(authenticatedUser.isAuthenticated()).isFalse();
        assertThat(authenticatedUser.getEmail()).isEmpty();
    }

    @Test
    void withNoLoginTheUserIsNotAuthenticated() {
        assertThat(authenticatedUser.isAuthenticated()).isFalse();
    }

    @Test
    void mustResetPasswordFlagIsCarriedFromTheTokenResponse() {
        String token = fakeJwt("jane@example.com", "USER", Instant.now().plusSeconds(600).getEpochSecond());
        authenticatedUser.login(new TokenResponse(token, "refresh-token", true, "Bearer"));

        assertThat(authenticatedUser.isMustResetPassword()).isTrue();
    }
}
