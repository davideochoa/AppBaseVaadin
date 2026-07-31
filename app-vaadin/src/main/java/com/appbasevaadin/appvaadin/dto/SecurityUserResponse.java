package com.appbasevaadin.appvaadin.dto;

public record SecurityUserResponse(String username, String email, String role, boolean active,
                                    boolean mustResetPassword) {
}
