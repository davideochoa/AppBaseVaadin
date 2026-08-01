package com.vaadinbaseapp.appvaadin.dto;

public record UserRequest(String firstName, String lastName, String email, Long userTypeId, Boolean active) {
}
