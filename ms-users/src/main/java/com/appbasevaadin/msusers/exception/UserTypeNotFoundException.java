package com.appbasevaadin.msusers.exception;

public class UserTypeNotFoundException extends RuntimeException {

    public UserTypeNotFoundException(Long id) {
        super("No user type found with id " + id);
    }
}
