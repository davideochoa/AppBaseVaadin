package com.appbasevaadin.msusers.controller;

import com.appbasevaadin.msusers.dto.UserRequest;
import com.appbasevaadin.msusers.entity.UserType;
import com.appbasevaadin.msusers.repository.UserTypeRepository;
import com.appbasevaadin.msusers.support.PostgresTestContainerBase;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT extends PostgresTestContainerBase {

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
    private UserTypeRepository userTypeRepository;

    private Long userTypeId;

    @BeforeEach
    void setUp() {
        userTypeId = userTypeRepository.findAll().stream()
                .filter(t -> t.getName().equals("User"))
                .findFirst()
                .orElseGet(() -> {
                    UserType type = new UserType();
                    type.setName("User");
                    type.setDescription("Standard access");
                    return userTypeRepository.save(type);
                })
                .getId();
    }

    private UserRequest buildValidRequest() {
        UserRequest request = new UserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe." + System.nanoTime() + "@example.com");
        request.setUserTypeId(userTypeId);
        return request;
    }

    private HttpEntity<UserRequest> withAdminToken(UserRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<UserRequest> withUserToken(UserRequest body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("user-token");
        return new HttpEntity<>(body, headers);
    }

    @Test
    void fullUserCrudFlowAsAdmin() {
        UserRequest request = buildValidRequest();

        ResponseEntity<String> created = restTemplate.exchange(
                "/users", HttpMethod.POST, withAdminToken(request), String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).contains(request.getEmail());
    }

    @Test
    void createWithoutTokenReturns401() {
        UserRequest request = buildValidRequest();

        ResponseEntity<String> response = restTemplate.postForEntity("/users", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("UNAUTHORIZED");
    }

    @Test
    void createWithNonAdminTokenReturns403() {
        UserRequest request = buildValidRequest();

        ResponseEntity<String> response = restTemplate.exchange(
                "/users", HttpMethod.POST, withUserToken(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("FORBIDDEN");
    }

    @Test
    void createWithInvalidEmailReturns400() {
        UserRequest request = buildValidRequest();
        request.setEmail("not-an-email");

        ResponseEntity<String> response = restTemplate.exchange(
                "/users", HttpMethod.POST, withAdminToken(request), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("VALIDATION");
    }

    @Test
    void getNonexistentUserReturns404() {
        ResponseEntity<String> response = restTemplate.getForEntity("/users/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("NOT_FOUND");
    }

    @Test
    void createWithDuplicateEmailReturns409() {
        UserRequest request = buildValidRequest();
        restTemplate.exchange("/users", HttpMethod.POST, withAdminToken(request), String.class);

        ResponseEntity<String> secondResponse = restTemplate.exchange(
                "/users", HttpMethod.POST, withAdminToken(request), String.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void getByEmailFindsCreatedUser() {
        UserRequest request = buildValidRequest();
        restTemplate.exchange("/users", HttpMethod.POST, withAdminToken(request), String.class);

        ResponseEntity<String> response = restTemplate.getForEntity(
                "/users/by-email?email=" + request.getEmail(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(request.getEmail());
    }

    @Test
    void listUserTypesIsPublicAndIncludesAdministratorAndUser() {
        ResponseEntity<String> response = restTemplate.getForEntity("/user-types", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Administrator").contains("User");
    }
}
