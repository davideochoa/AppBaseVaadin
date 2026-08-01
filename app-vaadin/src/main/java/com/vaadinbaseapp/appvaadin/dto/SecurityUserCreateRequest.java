package com.vaadinbaseapp.appvaadin.dto;

public record SecurityUserCreateRequest(String username, String email, String role, Long userId) {
}
