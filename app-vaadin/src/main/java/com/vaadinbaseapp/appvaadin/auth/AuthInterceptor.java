package com.vaadinbaseapp.appvaadin.auth;

import com.vaadinbaseapp.appvaadin.client.AuthApiClient;
import com.vaadinbaseapp.appvaadin.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Attaches the session-held access token to outgoing requests and, on a 401,
 * refreshes it once and retries — per the security spec's "outgoing-call
 * interceptor... refreshes it on expiry before retrying."
 */
@Component
public class AuthInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final AuthenticatedUser authenticatedUser;
    private final AuthApiClient authApiClient;

    public AuthInterceptor(AuthenticatedUser authenticatedUser, AuthApiClient authApiClient) {
        this.authenticatedUser = authenticatedUser;
        this.authApiClient = authApiClient;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        authenticatedUser.getAccessToken().ifPresent(token -> request.getHeaders().setBearerAuth(token));

        ClientHttpResponse response = execution.execute(request, body);

        if (response.getStatusCode().value() != 401) {
            return response;
        }

        Object refreshToken = authenticatedUser.getRefreshToken().orElse(null);
        if (refreshToken == null) {
            return response;
        }

        try {
            TokenResponse refreshed = authApiClient.refresh((String) refreshToken);
            authenticatedUser.updateTokens(refreshed);
            request.getHeaders().setBearerAuth(refreshed.accessToken());
            return execution.execute(request, body);
        } catch (Exception e) {
            log.warn("Token refresh failed, clearing session", e);
            authenticatedUser.logout();
            return response;
        }
    }
}
