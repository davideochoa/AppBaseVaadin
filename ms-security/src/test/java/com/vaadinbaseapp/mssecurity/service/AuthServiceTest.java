package com.vaadinbaseapp.mssecurity.service;

import com.vaadinbaseapp.mssecurity.dto.TokenResponse;
import com.vaadinbaseapp.mssecurity.entity.AuthProvider;
import com.vaadinbaseapp.mssecurity.entity.RefreshToken;
import com.vaadinbaseapp.mssecurity.entity.SecurityUser;
import com.vaadinbaseapp.mssecurity.exception.InvalidCredentialsException;
import com.vaadinbaseapp.mssecurity.exception.InvalidRefreshTokenException;
import com.vaadinbaseapp.mssecurity.messaging.AuditEventPublisher;
import com.vaadinbaseapp.mssecurity.repository.RefreshTokenRepository;
import com.vaadinbaseapp.mssecurity.repository.SecurityUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SecurityUserRepository securityUserRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditEventPublisher auditEventPublisher;

    private AuthService authService;

    private SecurityUser user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(securityUserRepository, refreshTokenRepository, jwtService, passwordEncoder,
                auditEventPublisher);

        user = new SecurityUser();
        user.setId(1L);
        user.setUsername("jane.doe");
        user.setEmail("jane.doe@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole("USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
    }

    @Test
    void loginWithValidCredentialsIssuesTokens() {
        when(securityUserRepository.findByUsernameIgnoreCase("jane.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateOpaqueRefreshToken()).thenReturn("raw-refresh-token");
        when(jwtService.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");
        when(jwtService.getRefreshTokenTtlDays()).thenReturn(7L);

        TokenResponse response = authService.login("jane.doe", "correct-password", "127.0.0.1");

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("raw-refresh-token");
    }

    @Test
    void loginWithWrongPasswordThrows() {
        when(securityUserRepository.findByUsernameIgnoreCase("jane.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("jane.doe", "wrong-password", "127.0.0.1"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithUnknownUsernameThrows() {
        when(securityUserRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost", "whatever", "127.0.0.1"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginForGoogleOnlyAccountWithNoPasswordThrows() {
        user.setPasswordHash(null);
        when(securityUserRepository.findByUsernameIgnoreCase("jane.doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("jane.doe", "anything", "127.0.0.1"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refreshWithRevokedTokenThrows() {
        RefreshToken revoked = new RefreshToken();
        revoked.setTokenHash("hashed-refresh-token");
        revoked.setSecurityUserId(1L);
        revoked.setRevoked(true);
        revoked.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(jwtService.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");
        when(refreshTokenRepository.findByTokenHash("hashed-refresh-token")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh("raw-refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshWithExpiredTokenThrows() {
        RefreshToken expired = new RefreshToken();
        expired.setTokenHash("hashed-refresh-token");
        expired.setSecurityUserId(1L);
        expired.setRevoked(false);
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(jwtService.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");
        when(refreshTokenRepository.findByTokenHash("hashed-refresh-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("raw-refresh-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
