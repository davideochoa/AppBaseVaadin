package com.vaadinbaseapp.mssecurity.exception;

public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException() {
        super("Reset token is invalid or has expired");
    }
}
