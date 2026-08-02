package com.vaadinbaseapp.appvaadin.dto;

public record CompleteResetPasswordRequest(String resetToken, String newPassword) {
}
