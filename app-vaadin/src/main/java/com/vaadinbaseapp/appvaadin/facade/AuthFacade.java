package com.vaadinbaseapp.appvaadin.facade;

import com.vaadinbaseapp.appvaadin.auth.AuthenticatedUser;
import com.vaadinbaseapp.appvaadin.client.AuthApiClient;
import com.vaadinbaseapp.appvaadin.dto.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final AuthApiClient authApiClient;
    private final AuthenticatedUser authenticatedUser;

    public AuthFacade(AuthApiClient authApiClient, AuthenticatedUser authenticatedUser) {
        this.authApiClient = authApiClient;
        this.authenticatedUser = authenticatedUser;
    }

    public TokenResponse login(String username, String password) {
        return handleLoginResult(authApiClient.login(username, password));
    }

    public TokenResponse loginWithGoogle(String idToken) {
        return handleLoginResult(authApiClient.loginWithGoogle(idToken));
    }

    public void completePasswordReset(String resetToken, String newPassword) {
        authApiClient.completePasswordReset(resetToken, newPassword);
    }

    public void logout() {
        authenticatedUser.getRefreshToken().ifPresent(authApiClient::logout);
        authenticatedUser.logout();
    }

    /**
     * An account flagged mustResetPassword never gets a full session: the
     * server returns a resetToken instead of access/refresh tokens, so there
     * is nothing here to store in VaadinSession until the reset completes.
     */
    private TokenResponse handleLoginResult(TokenResponse tokens) {
        if (!tokens.mustResetPassword()) {
            authenticatedUser.login(tokens);
        }
        return tokens;
    }
}
