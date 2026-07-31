package com.appbasevaadin.mssecurity.controller;

import com.appbasevaadin.mssecurity.dto.ChangePasswordRequest;
import com.appbasevaadin.mssecurity.dto.SecurityUserCreateRequest;
import com.appbasevaadin.mssecurity.dto.SecurityUserResponse;
import com.appbasevaadin.mssecurity.dto.SecurityUserUpdateRequest;
import com.appbasevaadin.mssecurity.service.SecurityUserAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/security-users")
public class SecurityUserController {

    private final SecurityUserAdminService securityUserAdminService;

    public SecurityUserController(SecurityUserAdminService securityUserAdminService) {
        this.securityUserAdminService = securityUserAdminService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public SecurityUserResponse create(@Valid @RequestBody SecurityUserCreateRequest request) {
        return securityUserAdminService.create(request);
    }

    @PatchMapping("/{username}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public SecurityUserResponse update(@PathVariable String username,
                                        @Valid @RequestBody SecurityUserUpdateRequest request) {
        return securityUserAdminService.update(username, request);
    }

    @PostMapping("/{username}/reset-password")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public SecurityUserResponse resetPassword(@PathVariable String username) {
        return securityUserAdminService.resetPassword(username);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changeOwnPassword(@AuthenticationPrincipal Jwt jwt,
                                                    @Valid @RequestBody ChangePasswordRequest request) {
        securityUserAdminService.changeOwnPassword(jwt.getSubject(), request);
        return ResponseEntity.noContent().build();
    }
}
