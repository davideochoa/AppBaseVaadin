package com.appbasevaadin.mssecurity.service;

import com.appbasevaadin.mssecurity.client.UsersClient;
import com.appbasevaadin.mssecurity.client.dto.UserTypeDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class UserTypeCache {

    private static final String DEFAULT_NON_ADMIN_TYPE_NAME = "User";

    private final UsersClient usersClient;
    private final AtomicReference<Long> defaultUserTypeId = new AtomicReference<>();

    public UserTypeCache(UsersClient usersClient) {
        this.usersClient = usersClient;
    }

    public Long getDefaultNonAdminUserTypeId() {
        Long cached = defaultUserTypeId.get();
        if (cached != null) {
            return cached;
        }
        Long resolved = usersClient.listUserTypes().stream()
                .filter(type -> DEFAULT_NON_ADMIN_TYPE_NAME.equalsIgnoreCase(type.name()))
                .map(UserTypeDto::id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No '" + DEFAULT_NON_ADMIN_TYPE_NAME + "' user type found in ms-users"));
        defaultUserTypeId.set(resolved);
        return resolved;
    }
}
