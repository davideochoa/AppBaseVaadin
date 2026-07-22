package com.appbasevaadin.mssecurity.service;

import com.appbasevaadin.mssecurity.dto.TokenResponse;
import com.appbasevaadin.mssecurity.entity.RefreshToken;
import com.appbasevaadin.mssecurity.entity.SecurityUser;
import com.appbasevaadin.mssecurity.exception.InvalidCredentialsException;
import com.appbasevaadin.mssecurity.exception.InvalidRefreshTokenException;
import com.appbasevaadin.mssecurity.messaging.AuditEventPublisher;
import com.appbasevaadin.mssecurity.repository.RefreshTokenRepository;
import com.appbasevaadin.mssecurity.repository.SecurityUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthService {

    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditEventPublisher;

    public AuthService(SecurityUserRepository securityUserRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtService jwtService,
                        PasswordEncoder passwordEncoder,
                        AuditEventPublisher auditEventPublisher) {
        this.securityUserRepository = securityUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.auditEventPublisher = auditEventPublisher;
    }

    public TokenResponse login(String email, String rawPassword, String ipAddress) {
        SecurityUser user = securityUserRepository.findByEmailIgnoreCase(email)
                .filter(SecurityUser::isActive)
                .orElse(null);

        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            auditEventPublisher.publishLoginFailed(email, ipAddress);
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
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

        return new TokenResponse(accessToken, rawRefreshToken);
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
