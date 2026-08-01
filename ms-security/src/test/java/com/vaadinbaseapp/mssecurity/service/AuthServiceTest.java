package com.vaadinbaseapp.mssecurity.service;

import com.vaadinbaseapp.mssecurity.dto.TokenResponse;
import com.vaadinbaseapp.mssecurity.entity.AuthProvider;
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
    private PasswordResetTokenRepository passwordResetTokenRepository;

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
        authService = new AuthService(securityUserRepository, refreshTokenRepository, passwordResetTokenRepository,
                jwtService, passwordEncoder, auditEventPublisher);

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

    @Test
    void loginForAccountFlaggedMustResetPasswordReturnsAResetTokenInsteadOfASession() {
        user.setMustResetPassword(true);
        when(securityUserRepository.findByUsernameIgnoreCase("jane.doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.generateOpaqueRefreshToken()).thenReturn("raw-reset-token");
        when(jwtService.hash("raw-reset-token")).thenReturn("hashed-reset-token");

        TokenResponse response = authService.login("jane.doe", "correct-password", "127.0.0.1");

        assertThat(response.isMustResetPassword()).isTrue();
        assertThat(response.getResetToken()).isEqualTo("raw-reset-token");
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
    }

    @Test
    void completePasswordResetWithValidTokenAndDifferentPasswordSucceeds() {
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash("hashed-reset-token");
        token.setSecurityUserId(1L);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(jwtService.hash("raw-reset-token")).thenReturn("hashed-reset-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-reset-token")).thenReturn(Optional.of(token));
        when(securityUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("new-password", "hashed-password")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hashed-password");

        authService.completePasswordReset("raw-reset-token", "new-password");

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(user.isMustResetPassword()).isFalse();
    }

    @Test
    void completePasswordResetWithSamePasswordThrows() {
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenHash("hashed-reset-token");
        token.setSecurityUserId(1L);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(jwtService.hash("raw-reset-token")).thenReturn("hashed-reset-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-reset-token")).thenReturn(Optional.of(token));
        when(securityUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin", "hashed-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.completePasswordReset("raw-reset-token", "admin"))
                .isInstanceOf(SamePasswordException.class);
    }

    @Test
    void completePasswordResetWithExpiredTokenThrows() {
        PasswordResetToken expired = new PasswordResetToken();
        expired.setTokenHash("hashed-reset-token");
        expired.setSecurityUserId(1L);
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(jwtService.hash("raw-reset-token")).thenReturn("hashed-reset-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-reset-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.completePasswordReset("raw-reset-token", "new-password"))
                .isInstanceOf(InvalidResetTokenException.class);
    }

    @Test
    void completePasswordResetWithUnknownTokenThrows() {
        when(jwtService.hash("garbage")).thenReturn("hashed-garbage");
        when(passwordResetTokenRepository.findByTokenHash("hashed-garbage")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.completePasswordReset("garbage", "new-password"))
                .isInstanceOf(InvalidResetTokenException.class);
    }
}
