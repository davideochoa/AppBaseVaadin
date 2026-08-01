package com.vaadinbaseapp.mssecurity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteResetPasswordRequest {

    @NotBlank(message = "resetToken is required")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 4, max = 100, message = "New password must be between 4 and 100 characters")
    private String newPassword;
}
