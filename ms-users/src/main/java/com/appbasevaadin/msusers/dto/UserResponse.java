package com.appbasevaadin.msusers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Plain response payload — mapping from {@code User} lives in {@link com.appbasevaadin.msusers.mapper.UserMapper},
 * not here, so this class stays a pure data holder.
 */
@Getter
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final UserTypeResponse userType;
}
