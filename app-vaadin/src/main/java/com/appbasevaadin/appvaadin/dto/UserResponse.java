package com.appbasevaadin.appvaadin.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String firstName, String lastName, String email, boolean active,
                            LocalDateTime createdAt, UserTypeResponse userType) {
}
