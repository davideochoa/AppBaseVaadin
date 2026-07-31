package com.appbasevaadin.appvaadin.client;

import com.appbasevaadin.appvaadin.dto.ChangePasswordRequest;
import com.appbasevaadin.appvaadin.dto.SecurityUserCreateRequest;
import com.appbasevaadin.appvaadin.dto.SecurityUserResponse;
import com.appbasevaadin.appvaadin.dto.SecurityUserUpdateRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SecurityUserApiClient {

    private final RestClient securityAdminRestClient;

    public SecurityUserApiClient(RestClient securityAdminRestClient) {
        this.securityAdminRestClient = securityAdminRestClient;
    }

    public SecurityUserResponse create(SecurityUserCreateRequest request) {
        return securityAdminRestClient.post()
                .uri("/security-users")
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(SecurityUserResponse.class);
    }

    public SecurityUserResponse update(String username, SecurityUserUpdateRequest request) {
        return securityAdminRestClient.patch()
                .uri("/security-users/{username}", username)
                .body(request)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(SecurityUserResponse.class);
    }

    public SecurityUserResponse resetPassword(String username) {
        return securityAdminRestClient.post()
                .uri("/security-users/{username}/reset-password", username)
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .body(SecurityUserResponse.class);
    }

    public void changeOwnPassword(String newPassword) {
        securityAdminRestClient.post()
                .uri("/security-users/me/change-password")
                .body(new ChangePasswordRequest(newPassword))
                .retrieve()
                .onStatus(status -> status.value() >= 400, ApiClientSupport::handleError)
                .toBodilessEntity();
    }
}
