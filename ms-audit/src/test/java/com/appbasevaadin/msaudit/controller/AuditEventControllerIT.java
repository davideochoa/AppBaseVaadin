package com.appbasevaadin.msaudit.controller;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import com.appbasevaadin.msaudit.repository.AuditEventRepository;
import com.appbasevaadin.msaudit.support.FullStackTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuditEventControllerIT extends FullStackTestContainerBase {

    @TestConfiguration
    static class JwtTestConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> switch (token) {
                case "admin-token" -> buildJwt(token, "ADMINISTRATOR");
                case "user-token" -> buildJwt(token, "USER");
                default -> throw new BadJwtException("Unknown test token");
            };
        }

        private Jwt buildJwt(String token, String role) {
            Instant now = Instant.now();
            return Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test@example.com")
                    .claim("role", role)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(3600))
                    .build();
        }
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        AuditEvent event = new AuditEvent();
        event.setType("LOGIN_FAILED");
        event.setEmail("jane.doe@example.com");
        event.setIpAddress("203.0.113.7");
        event.setOccurredAt(LocalDateTime.now());
        auditEventRepository.save(event);
    }

    private HttpEntity<Void> withToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    @Test
    void listWithoutTokenReturns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/audit-events", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("UNAUTHORIZED");
    }

    @Test
    void listWithNonAdminTokenReturns403() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/audit-events", HttpMethod.GET, withToken("user-token"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("FORBIDDEN");
    }

    @Test
    void listWithAdminTokenReturnsSeededEvent() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/audit-events", HttpMethod.GET, withToken("admin-token"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("LOGIN_FAILED").contains("jane.doe@example.com");
    }

    @Test
    void listNeverExposesAPassword() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/audit-events", HttpMethod.GET, withToken("admin-token"), String.class);

        assertThat(response.getBody()).doesNotContainIgnoringCase("password");
    }
}
