package com.appbasevaadin.mssecurity.controller;

import com.appbasevaadin.mssecurity.dto.GoogleLoginRequest;
import com.appbasevaadin.mssecurity.dto.LoginRequest;
import com.appbasevaadin.mssecurity.dto.RefreshRequest;
import com.appbasevaadin.mssecurity.dto.TokenResponse;
import com.appbasevaadin.mssecurity.service.AuthService;
import com.appbasevaadin.mssecurity.service.GoogleLoginService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final GoogleLoginService googleLoginService;

    public AuthController(AuthService authService, GoogleLoginService googleLoginService) {
        this.authService = authService;
        this.googleLoginService = googleLoginService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/login/google")
    public TokenResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return googleLoginService.login(request.getIdToken());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
