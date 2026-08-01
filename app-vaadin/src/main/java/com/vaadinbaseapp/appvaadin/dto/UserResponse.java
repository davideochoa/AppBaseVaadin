package com.vaadinbaseapp.appvaadin.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String username, String firstName, String lastName, String email,
                            boolean active, LocalDateTime createdAt, UserTypeResponse userType) {
}
