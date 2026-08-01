package com.vaadinbaseapp.mssecurity.exception;

public class SecurityUserNotFoundException extends RuntimeException {

    public SecurityUserNotFoundException(String username) {
        super("No security user found for username '" + username + "'");
    }
}
