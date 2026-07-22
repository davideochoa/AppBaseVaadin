package com.appbasevaadin.mssecurity.service;

import com.appbasevaadin.mssecurity.dto.TokenResponse;
import com.appbasevaadin.mssecurity.entity.AuthProvider;
import com.appbasevaadin.mssecurity.entity.RefreshToken;
import com.appbasevaadin.mssecurity.entity.SecurityUser;
import com.appbasevaadin.mssecurity.exception.InvalidCredentialsException;
import com.appbasevaadin.mssecurity.exception.InvalidRefreshTokenException;
import com.appbasevaadin.mssecurity.repository.RefreshTokenRepository;
import com.appbasevaadin.mssecurity.repository.SecurityUserRepository;
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

    private AuthService authService;

    private SecurityUser user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(securityUserRepository, refreshTokenRepository, jwtService, passwordEncoder);

        user = new SecurityUser();
        user.setId(1L);
        user.setEmail("jane.doe@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole("USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setActive(true);
    }

    @Test
    void loginWithValidCredentialsIssuesTokens() {
        when(securityUserRepository.findByEmailIgnoreCase("jane.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateOpaqueRefreshToken()).thenReturn("raw-refresh-token");
        when(jwtService.hash("raw-refresh-token")).thenReturn("hashed-refresh-token");
        when(jwtService.getRefreshTokenTtlDays()).thenReturn(7L);

        TokenResponse response = authService.login("jane.doe@example.com", "correct-password");

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("raw-refresh-token");
    }

    @Test
    void loginWithWrongPasswordThrows() {
        when(securityUserRepository.findByEmailIgnoreCase("jane.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("jane.doe@example.com", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithUnknownEmailThrows() {
        when(securityUserRepository.findByEmailIgnoreCase("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("ghost@example.com", "whatever"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginForGoogleOnlyAccountWithNoPasswordThrows() {
        user.setPasswordHash(null);
        when(securityUserRepository.findByEmailIgnoreCase("jane.doe@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login("jane.doe@example.com", "anything"))
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
