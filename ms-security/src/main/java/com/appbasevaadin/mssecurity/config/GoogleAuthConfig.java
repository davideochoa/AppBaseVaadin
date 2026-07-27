package com.appbasevaadin.mssecurity.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Builds the {@link GoogleIdTokenVerifier} used to validate Google id-tokens, so
 * {@link com.appbasevaadin.mssecurity.service.GoogleLoginService} receives an already-configured
 * verifier instead of wiring up the third-party builder itself — keeps the service focused on
 * the login flow, not on how the verifier is assembled.
 */
@Configuration
public class GoogleAuthConfig {

    @Bean
    public GoogleIdTokenVerifier googleIdTokenVerifier(@Value("${app.google.client-id}") String googleClientId) {
        return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }
}
