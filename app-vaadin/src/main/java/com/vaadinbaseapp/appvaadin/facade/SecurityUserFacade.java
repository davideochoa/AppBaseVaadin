package com.vaadinbaseapp.appvaadin.facade;

import com.vaadinbaseapp.appvaadin.client.SecurityUserApiClient;
import com.vaadinbaseapp.appvaadin.dto.SecurityUserCreateRequest;
import com.vaadinbaseapp.appvaadin.dto.SecurityUserResponse;
import com.vaadinbaseapp.appvaadin.dto.SecurityUserUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserFacade {

    private final SecurityUserApiClient securityUserApiClient;

    public SecurityUserFacade(SecurityUserApiClient securityUserApiClient) {
        this.securityUserApiClient = securityUserApiClient;
    }

    public SecurityUserResponse create(String username, String email, String role, Long userId) {
        return securityUserApiClient.create(new SecurityUserCreateRequest(username, email, role, userId));
    }

    public SecurityUserResponse update(String currentUsername, String newUsername, String email, String role,
                                        boolean active) {
        return securityUserApiClient.update(currentUsername,
                new SecurityUserUpdateRequest(newUsername, email, role, active));
    }

    public void resetPassword(String username) {
        securityUserApiClient.resetPassword(username);
    }

    public void changeOwnPassword(String newPassword) {
        securityUserApiClient.changeOwnPassword(newPassword);
    }
}
