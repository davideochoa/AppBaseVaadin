package com.vaadinbaseapp.mssecurity.service;

import com.vaadinbaseapp.mssecurity.dto.TokenResponse;
import com.vaadinbaseapp.mssecurity.entity.PasswordResetToken;
import com.vaadinbaseapp.mssecurity.entity.RefreshToken;
import com.vaadinbaseapp.mssecurity.entity.SecurityUser;
import com.vaadinbaseapp.mssecurity.exception.InvalidCredentialsException;
import com.vaadinbaseapp.mssecurity.exception.InvalidRefreshTokenException;
import com.vaadinbaseapp.mssecurity.exception.InvalidResetTokenException;
import com.vaadinbaseapp.mssecurity.exception.SamePasswordException;
import com.vaadinbaseapp.mssecurity.messaging.AuditEventPublisher;
import com.vaadinbaseapp.mssecurity.repository.PasswordResetTokenRepository;
import com.vaadinbaseapp.mssecurity.repository.RefreshTokenRepository;
import com.vaadinbaseapp.mssecurity.repository.SecurityUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private static final long RESET_TOKEN_TTL_MINUTES = 10;

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditEventPublisher;

    public AuthService(SecurityUserRepository securityUserRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        PasswordResetTokenRepository passwordResetTokenRepository,
                        JwtService jwtService,
                        PasswordEncoder passwordEncoder,
                        AuditEventPublisher auditEventPublisher) {
        this.securityUserRepository = securityUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.auditEventPublisher = auditEventPublisher;
    }

    /**
     * The must_reset_password check only runs here, at login time (never on
     * an already-issued token pair) — an account flagged mid-session keeps
     * its current session working until it logs out and back in.
     */
    public TokenResponse login(String username, String rawPassword, String ipAddress) {
        SecurityUser user = securityUserRepository.findByUsernameIgnoreCase(username)
                .filter(SecurityUser::isActive)
                .orElse(null);

        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            auditEventPublisher.publishLoginFailed(username, ipAddress);
            throw new InvalidCredentialsException();
        }

        if (user.isMustResetPassword()) {
            return new TokenResponse(null, null, true, issuePasswordResetToken(user));
        }

        return issueTokens(user);
    }

    private String issuePasswordResetToken(SecurityUser user) {
        passwordResetTokenRepository.deleteBySecurityUserId(user.getId());

        String rawToken = jwtService.generateOpaqueRefreshToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash(jwtService.hash(rawToken));
        token.setSecurityUserId(user.getId());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES));
        passwordResetTokenRepository.save(token);
        return rawToken;
    }

    /**
     * Completes the forced login-time reset: no JWT is required (the caller
     * never got a full session), the single-use resetToken stands in for one.
     * Distinct from SecurityUserAdminService#changeOwnPassword, which is the
     * general/voluntary self-service change for an already-authenticated user.
     */
    public void completePasswordReset(String rawResetToken, String newPassword) {
        String hash = jwtService.hash(rawResetToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidResetTokenException::new);

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            throw new InvalidResetTokenException();
        }

        SecurityUser user = securityUserRepository.findById(token.getSecurityUserId())
                .orElseThrow(InvalidResetTokenException::new);

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new SamePasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustResetPassword(false);
        securityUserRepository.save(user);
        passwordResetTokenRepository.delete(token);
    }

    public TokenResponse refresh(String rawRefreshToken) {
        RefreshToken stored = findValidRefreshToken(rawRefreshToken);
        SecurityUser user = securityUserRepository.findById(stored.getSecurityUserId())
                .filter(SecurityUser::isActive)
                .orElseThrow(InvalidRefreshTokenException::new);

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user);
    }

    public void logout(String rawRefreshToken) {
        String hash = jwtService.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public TokenResponse issueTokens(SecurityUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateOpaqueRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(jwtService.hash(rawRefreshToken));
        refreshToken.setSecurityUserId(user.getId());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(jwtService.getRefreshTokenTtlDays()));
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, rawRefreshToken, false, null);
    }

    private RefreshToken findValidRefreshToken(String rawRefreshToken) {
        String hash = jwtService.hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }
        return stored;
    }
}
