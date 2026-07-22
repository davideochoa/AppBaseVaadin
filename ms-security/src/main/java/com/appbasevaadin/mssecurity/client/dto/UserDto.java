package com.appbasevaadin.mssecurity.client.dto;

public record UserDto(Long id, String firstName, String lastName, String email,
                       boolean active, UserTypeDto userType) {
}
