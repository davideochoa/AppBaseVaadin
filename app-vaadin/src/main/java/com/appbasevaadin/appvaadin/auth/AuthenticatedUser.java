package com.appbasevaadin.appvaadin.auth;

import com.appbasevaadin.appvaadin.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Holds the JWT pair server-side in {@link VaadinSession} (never browser storage, per the
 * security spec). Claims are decoded locally for UI gating only — the actual trust boundary
 * is each microservice's own resource-server JWT validation against ms-security's JWKS; this
 * class never re-verifies the signature, since app-vaadin just received the token directly
 * from ms-security moments earlier.
 */
@Component
public class AuthenticatedUser {

    private static final String SESSION_KEY = AuthenticatedUser.class.getName();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public void login(TokenResponse tokens) {
        VaadinSession.getCurrent().setAttribute(SESSION_KEY, new Session(tokens, decodeClaims(tokens.accessToken())));
    }

    public void updateTokens(TokenResponse tokens) {
        login(tokens);
    }

    public void logout() {
        VaadinSession.getCurrent().setAttribute(SESSION_KEY, null);
    }

    public boolean isAuthenticated() {
        return currentSession().filter(this::isNotExpired).isPresent();
    }

    public Optional<String> getAccessToken() {
        return currentSession().map(Session::tokens).map(TokenResponse::accessToken);
    }

    public Optional<String> getRefreshToken() {
        return currentSession().map(Session::tokens).map(TokenResponse::refreshToken);
    }

    public Optional<String> getEmail() {
        return currentSession().map(Session::claims).map(claims -> (String) claims.get("email"));
    }

    public Optional<String> getUsername() {
        return currentSession().map(Session::claims).map(claims -> (String) claims.get("username"));
    }

    public boolean isMustResetPassword() {
        return currentSession().map(Session::tokens).map(TokenResponse::mustResetPassword).orElse(false);
    }

    public Optional<String> getRole() {
        return currentSession().map(Session::claims).map(claims -> (String) claims.get("role"));
    }

    public boolean hasRole(String role) {
        return getRole().map(r -> r.equalsIgnoreCase(role)).orElse(false);
    }

    private boolean isNotExpired(Session session) {
        Object exp = session.claims().get("exp");
        if (exp == null) {
            return false;
        }
        long expSeconds = ((Number) exp).longValue();
        return Instant.now().isBefore(Instant.ofEpochSecond(expSeconds));
    }

    private Optional<Session> currentSession() {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Session) vaadinSession.getAttribute(SESSION_KEY));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeClaims(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            return OBJECT_MAPPER.readValue(new String(payload, StandardCharsets.UTF_8), Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private record Session(TokenResponse tokens, Map<String, Object> claims) {
    }
}
