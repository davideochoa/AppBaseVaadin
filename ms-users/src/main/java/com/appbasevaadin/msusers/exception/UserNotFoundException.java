package com.appbasevaadin.msusers.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("No user found with id " + id);
    }

    public static UserNotFoundException forEmail(String email) {
        return new UserNotFoundException("No user found with email " + email);
    }

    private UserNotFoundException(String message) {
        super(message);
    }
}
