package com.appbasevaadin.mssecurity.controller;

import com.appbasevaadin.mssecurity.dto.GoogleLoginRequest;
import com.appbasevaadin.mssecurity.dto.LoginRequest;
import com.appbasevaadin.mssecurity.dto.RefreshRequest;
import com.appbasevaadin.mssecurity.dto.TokenResponse;
import com.appbasevaadin.mssecurity.entity.AuthProvider;
import com.appbasevaadin.mssecurity.entity.SecurityUser;
import com.appbasevaadin.mssecurity.repository.SecurityUserRepository;
import com.appbasevaadin.mssecurity.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIT extends PostgresTestContainerBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String email;

    @BeforeEach
    void setUp() {
        email = "jane.doe." + System.nanoTime() + "@example.com";
        SecurityUser user = new SecurityUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("correct-password"));
        user.setRole("USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
        securityUserRepository.save(user);
    }

    private LoginRequest loginRequest(String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    @Test
    void loginWithValidCredentialsReturnsTokens() {
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                "/login", loginRequest("correct-password"), TokenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccessToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    @Test
    void loginWithWrongPasswordReturns401() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/login", loginRequest("wrong-password"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("UNAUTHORIZED");
    }

    @Test
    void refreshIssuesNewTokenAndRevokesThePreviousOne() {
        TokenResponse initial = restTemplate.postForEntity(
                "/login", loginRequest("correct-password"), TokenResponse.class).getBody();

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(initial.getRefreshToken());
        ResponseEntity<TokenResponse> refreshed = restTemplate.postForEntity(
                "/refresh", refreshRequest, TokenResponse.class);

        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshed.getBody().getRefreshToken()).isNotEqualTo(initial.getRefreshToken());

        ResponseEntity<String> reuseAttempt = restTemplate.postForEntity("/refresh", refreshRequest, String.class);
        assertThat(reuseAttempt.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutRevokesTheRefreshToken() {
        TokenResponse initial = restTemplate.postForEntity(
                "/login", loginRequest("correct-password"), TokenResponse.class).getBody();

        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(initial.getRefreshToken());
        ResponseEntity<Void> logoutResponse = restTemplate.postForEntity("/logout", refreshRequest, Void.class);
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> reuseAttempt = restTemplate.postForEntity("/refresh", refreshRequest, String.class);
        assertThat(reuseAttempt.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void googleLoginWithInvalidIdTokenReturns401() {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken("not-a-real-google-token");

        ResponseEntity<String> response = restTemplate.postForEntity("/login/google", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("UNAUTHORIZED");
    }

    @Test
    void jwksEndpointIsPublicAndReturnsAKeySet() {
        ResponseEntity<String> response = restTemplate.getForEntity("/.well-known/jwks.json", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"keys\"");
    }
}
