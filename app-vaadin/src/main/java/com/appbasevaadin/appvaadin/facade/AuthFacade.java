package com.appbasevaadin.appvaadin.facade;

import com.appbasevaadin.appvaadin.auth.AuthenticatedUser;
import com.appbasevaadin.appvaadin.client.AuthApiClient;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final AuthApiClient authApiClient;
    private final AuthenticatedUser authenticatedUser;

    public AuthFacade(AuthApiClient authApiClient, AuthenticatedUser authenticatedUser) {
        this.authApiClient = authApiClient;
        this.authenticatedUser = authenticatedUser;
    }

    public void login(String email, String password) {
        authenticatedUser.login(authApiClient.login(email, password));
    }

    public void loginWithGoogle(String idToken) {
        authenticatedUser.login(authApiClient.loginWithGoogle(idToken));
    }

    public void logout() {
        authenticatedUser.getRefreshToken().ifPresent(authApiClient::logout);
        authenticatedUser.logout();
    }
}
