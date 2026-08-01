package com.vaadinbaseapp.appvaadin.client;

import com.vaadinbaseapp.appvaadin.dto.PageResponse;
import com.vaadinbaseapp.appvaadin.dto.UserRequest;
import com.vaadinbaseapp.appvaadin.dto.UserResponse;
import com.vaadinbaseapp.appvaadin.dto.UserTypeRequest;
import com.vaadinbaseapp.appvaadin.dto.UserTypeResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class UsersApiClient {

    private final RestClient usersRestClient;

    public UsersApiClient(RestClient usersRestClient) {
        this.usersRestClient = usersRestClient;
    }

    public PageResponse<UserResponse> search(String text, Long userTypeId, Boolean active, int page, int size) {
        return usersRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/users").queryParam("page", page).queryParam("size", size);
                    if (text != null && !text.isBlank()) {
                        uriBuilder.queryParam("text", text);
                    }
                    if (userTypeId != null) {
                        uriBuilder.queryParam("userTypeId", userTypeId);
                    }
                    if (active != null) {
                        uriBuilder.queryParam("active", active);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(new ParameterizedTypeReference<PageResponse<UserResponse>>() {
                });
    }

    public UserResponse getById(Long id) {
        return usersRestClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(UserResponse.class);
    }

    public UserResponse getByEmail(String email) {
        return usersRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users/by-email").queryParam("email", email).build())
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(UserResponse.class);
    }

    public UserResponse create(UserRequest request) {
        return usersRestClient.post()
                .uri("/users")
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(UserResponse.class);
    }

    public UserResponse update(Long id, UserRequest request) {
        return usersRestClient.put()
                .uri("/users/{id}", id)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(UserResponse.class);
    }

    public void hardDelete(Long id) {
        usersRestClient.delete()
                .uri("/users/{id}/hard", id)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .toBodilessEntity();
    }

    public List<UserTypeResponse> listUserTypes() {
        return usersRestClient.get()
                .uri("/user-types")
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(new ParameterizedTypeReference<List<UserTypeResponse>>() {
                });
    }

    public UserTypeResponse createUserType(UserTypeRequest request) {
        return usersRestClient.post()
                .uri("/user-types")
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(UserTypeResponse.class);
    }

    public UserTypeResponse updateUserType(Long id, UserTypeRequest request) {
        return usersRestClient.put()
                .uri("/user-types/{id}", id)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(UserTypeResponse.class);
    }
}
