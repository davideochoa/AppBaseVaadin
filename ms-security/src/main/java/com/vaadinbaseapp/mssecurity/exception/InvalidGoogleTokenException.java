package com.vaadinbaseapp.mssecurity.exception;

public class InvalidGoogleTokenException extends RuntimeException {

    public InvalidGoogleTokenException(String reason) {
        super("Invalid Google id-token: " + reason);
    }
}
