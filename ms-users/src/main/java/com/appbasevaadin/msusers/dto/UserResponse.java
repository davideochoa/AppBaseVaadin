package com.appbasevaadin.msusers.dto;

import com.appbasevaadin.msusers.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final UserTypeResponse userType;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),
                user.getCreatedAt(),
                UserTypeResponse.from(user.getUserType())
        );
    }
}
