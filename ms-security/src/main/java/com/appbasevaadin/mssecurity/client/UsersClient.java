package com.appbasevaadin.mssecurity.client;

import com.appbasevaadin.mssecurity.client.dto.UserTypeDto;
import com.appbasevaadin.mssecurity.client.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class UsersClient {

    private final RestClient restClient;

    public UsersClient(@Value("${app.clients.ms-users-base-url}") String msUsersBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(msUsersBaseUrl).build();
    }

    public Optional<UserDto> findByEmail(String email) {
        try {
            UserDto user = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users/by-email").queryParam("email", email).build())
                    .retrieve()
                    .body(UserDto.class);
            return Optional.ofNullable(user);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public UserDto create(String firstName, String lastName, String email, Long userTypeId) {
        return restClient.post()
                .uri("/users")
                .body(new CreateUserRequest(firstName, lastName, email, userTypeId, true))
                .retrieve()
                .body(UserDto.class);
    }

    public List<UserTypeDto> listUserTypes() {
        return restClient.get()
                .uri("/user-types")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<UserTypeDto>>() {
                });
    }

    private record CreateUserRequest(String firstName, String lastName, String email,
                                      Long userTypeId, boolean active) {
    }
}
