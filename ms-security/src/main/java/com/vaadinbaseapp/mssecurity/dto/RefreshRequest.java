package com.vaadinbaseapp.mssecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {

    @NotBlank(message = "refreshToken is required")
    private String refreshToken;
}
