package com.vaadinbaseapp.mssecurity.client;

import com.vaadinbaseapp.mssecurity.client.dto.UserDto;
import com.vaadinbaseapp.mssecurity.client.dto.UserTypeDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class UsersClient {

    private final RestClient restClient;

    public UsersClient(RestClient usersRestClient) {
        this.restClient = usersRestClient;
    }

    public Optional<UserDto> findByEmail(String email) {
        try {
            UserDto user = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users/by-email").queryParam("email", email).build())
                    .retrieve()
                    .body(UserDto.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public UserDto create(String username, String firstName, String lastName, String email, Long userTypeId) {
        return restClient.post()
                .uri("/users")
                .body(new CreateUserRequest(username, firstName, lastName, email, userTypeId, true))
                .retrieve()
                .body(UserDto.class);
    }

    public List<UserTypeDto> listUserTypes() {
        return restClient.get()
                .uri("/user-types")
                .retrieve()
                .body(new ParameterizedTypeReference<List<UserTypeDto>>() {
                });
    }

    private record CreateUserRequest(String username, String firstName, String lastName, String email,
                                      Long userTypeId, boolean active) {
    }
}
