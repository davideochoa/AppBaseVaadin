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

import java.security.SecureRandom;

/**
 * Admin-driven credential management, kept separate from {@link AuthService}
 * (login/refresh/logout) since it's a different concern: creating/editing the
 * user_security row itself rather than issuing tokens for an existing one.
 */
@Service
@Transactional
public class SecurityUserAdminService {

    /**
     * Excludes visually ambiguous characters (0/O, 1/l/I) since this string is
     * meant to be relayed by an admin to a user over chat/phone/etc.
     */
    private static final String TEMP_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int TEMP_PASSWORD_LENGTH = 16;

    private final SecurityUserRepository securityUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public SecurityUserAdminService(SecurityUserRepository securityUserRepository,
                                     PasswordEncoder passwordEncoder) {
        this.securityUserRepository = securityUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Default password is a random, one-time temporary password (never the
     * username — that's guessable by anyone who knows/derives the username)
     * — the admin never types a password for a newly created user, and the
     * generated value is returned once so the admin can relay it to the new
     * user out-of-band. The account is flagged so the very next login forces
     * the user to pick a real one.
     */
    public SecurityUserResponse create(SecurityUserCreateRequest request) {
        SecurityUser user = new SecurityUser();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setUserId(request.getUserId());
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
        String temporaryPassword = generateTemporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustResetPassword(true);
        securityUserRepository.save(user);
        return toResponse(user, temporaryPassword);
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
     * Resets the password to a fresh random temporary one (see {@link #create})
     * and forces a reset on the next login — the same default state as a
     * freshly created user.
     */
    public SecurityUserResponse resetPassword(String username) {
        SecurityUser user = findByUsername(username);
        String temporaryPassword = generateTemporaryPassword();
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustResetPassword(true);
        securityUserRepository.save(user);
        return toResponse(user, temporaryPassword);
    }

    private String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            password.append(TEMP_PASSWORD_ALPHABET.charAt(secureRandom.nextInt(TEMP_PASSWORD_ALPHABET.length())));
        }
        return password.toString();
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
        return toResponse(user, null);
    }

    private SecurityUserResponse toResponse(SecurityUser user, String temporaryPassword) {
        return new SecurityUserResponse(user.getUsername(), user.getEmail(), user.getRole(),
                user.isActive(), user.isMustResetPassword(), temporaryPassword);
    }
}
