package com.vaadinbaseapp.mssecurity.service;

import com.vaadinbaseapp.mssecurity.dto.ChangePasswordRequest;
import com.vaadinbaseapp.mssecurity.dto.SecurityUserCreateRequest;
import com.vaadinbaseapp.mssecurity.dto.SecurityUserResponse;
import com.vaadinbaseapp.mssecurity.dto.SecurityUserUpdateRequest;
import com.vaadinbaseapp.mssecurity.entity.AuthProvider;
import com.vaadinbaseapp.mssecurity.entity.SecurityUser;
import com.vaadinbaseapp.mssecurity.exception.SecurityUserNotFoundException;
import com.vaadinbaseapp.mssecurity.repository.SecurityUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-driven credential management, kept separate from {@link AuthService}
 * (login/refresh/logout) since it's a different concern: creating/editing the
 * user_security row itself rather than issuing tokens for an existing one.
 */
@Service
@Transactional
public class SecurityUserAdminService {

    private final SecurityUserRepository securityUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityUserAdminService(SecurityUserRepository securityUserRepository,
                                     PasswordEncoder passwordEncoder) {
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Default password is the username itself, encrypted — the admin never
     * types a password for a newly created user. The account is flagged so
     * the very next login forces the user to pick a real one.
     */
    public SecurityUserResponse create(SecurityUserCreateRequest request) {
        SecurityUser user = new SecurityUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setUserId(request.getUserId());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.getUsername()));
        user.setMustResetPassword(true);
        securityUserRepository.save(user);
        return toResponse(user);
    }

    public SecurityUserResponse update(String username, SecurityUserUpdateRequest request) {
        SecurityUser user = findByUsername(username);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setActive(request.getActive());
        securityUserRepository.save(user);
        return toResponse(user);
    }

    /**
     * Resets the password back to the (possibly just-renamed) username,
     * encrypted, and forces a reset on the next login — the same default
     * state as a freshly created user.
     */
    public SecurityUserResponse resetPassword(String username) {
        SecurityUser user = findByUsername(username);
        user.setPasswordHash(passwordEncoder.encode(user.getUsername()));
        user.setMustResetPassword(true);
        securityUserRepository.save(user);
        return toResponse(user);
    }

    /**
     * Self-service: the caller sets their own new password, identified by
     * the username embedded in their own JWT (never another user's).
     */
    public void changeOwnPassword(String username, ChangePasswordRequest request) {
        SecurityUser user = findByUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustResetPassword(false);
        securityUserRepository.save(user);
    }

    private SecurityUser findByUsername(String username) {
        return securityUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new SecurityUserNotFoundException(username));
    }

    private SecurityUserResponse toResponse(SecurityUser user) {
        return new SecurityUserResponse(user.getUsername(), user.getEmail(), user.getRole(),
                user.isActive(), user.isMustResetPassword());
    }
}
