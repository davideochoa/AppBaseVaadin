package com.appbasevaadin.appvaadin.dto;

public record SecurityUserUpdateRequest(String username, String email, String role, boolean active) {
}
