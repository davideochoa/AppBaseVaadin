package com.vaadinbaseapp.mssecurity.controller;

import com.vaadinbaseapp.mssecurity.config.ClientIpResolver;
import com.vaadinbaseapp.mssecurity.dto.GoogleLoginRequest;
import com.vaadinbaseapp.mssecurity.dto.LoginRequest;
import com.vaadinbaseapp.mssecurity.dto.RefreshRequest;
import com.vaadinbaseapp.mssecurity.dto.TokenResponse;
import com.vaadinbaseapp.mssecurity.service.AuthService;
import com.vaadinbaseapp.mssecurity.service.GoogleLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final GoogleLoginService googleLoginService;
    private final ClientIpResolver clientIpResolver;

    public AuthController(AuthService authService, GoogleLoginService googleLoginService,
                           ClientIpResolver clientIpResolver) {
        this.authService = authService;
        this.googleLoginService = googleLoginService;
        this.clientIpResolver = clientIpResolver;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request.getUsername(), request.getPassword(), clientIpResolver.resolve(httpRequest));
    }

    @PostMapping("/login/google")
    public TokenResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request,
                                          HttpServletRequest httpRequest) {
        return googleLoginService.login(request.getIdToken(), clientIpResolver.resolve(httpRequest));
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
