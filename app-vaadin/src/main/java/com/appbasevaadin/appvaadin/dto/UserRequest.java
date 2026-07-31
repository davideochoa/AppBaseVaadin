package com.appbasevaadin.appvaadin.dto;

public record UserRequest(String username, String firstName, String lastName, String email, Long userTypeId,
                           Boolean active) {
}
