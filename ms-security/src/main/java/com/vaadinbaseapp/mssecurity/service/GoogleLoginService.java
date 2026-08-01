package com.vaadinbaseapp.mssecurity.service;

import com.vaadinbaseapp.mssecurity.client.UsersClient;
import com.vaadinbaseapp.mssecurity.client.dto.UserDto;
import com.vaadinbaseapp.mssecurity.dto.TokenResponse;
import com.vaadinbaseapp.mssecurity.entity.AuthProvider;
import com.vaadinbaseapp.mssecurity.entity.SecurityUser;
import com.vaadinbaseapp.mssecurity.exception.InvalidGoogleTokenException;
import com.vaadinbaseapp.mssecurity.messaging.AuditEventPublisher;
import com.vaadinbaseapp.mssecurity.repository.SecurityUserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;

@Service
public class GoogleLoginService {

    private static final String DEFAULT_ROLE = "USER";

    private final GoogleIdTokenVerifier verifier;
    private final SecurityUserRepository securityUserRepository;
    private final UsersClient usersClient;
    private final UserTypeCache userTypeCache;
    private final AuthService authService;
    private final AuditEventPublisher auditEventPublisher;

    public GoogleLoginService(GoogleIdTokenVerifier verifier,
                               SecurityUserRepository securityUserRepository,
                               UsersClient usersClient,
                               UserTypeCache userTypeCache,
                               AuthService authService,
                               AuditEventPublisher auditEventPublisher) {
        this.verifier = verifier;
        this.securityUserRepository = securityUserRepository;
        this.usersClient = usersClient;
        this.userTypeCache = userTypeCache;
        this.authService = authService;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public TokenResponse login(String rawIdToken, String ipAddress) {
        GoogleIdToken.Payload payload = verify(rawIdToken, ipAddress);
        String email = payload.getEmail();

        SecurityUser securityUser = securityUserRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> provisionNewGoogleUser(email, payload));

        return authService.issueTokens(securityUser);
    }

    private SecurityUser provisionNewGoogleUser(String email, GoogleIdToken.Payload payload) {
        UserDto user = usersClient.findByEmail(email)
                .orElseGet(() -> createUserProfile(email, payload));

        SecurityUser securityUser = new SecurityUser();
        securityUser.setUserId(user.id());
        securityUser.setUsername(deriveUsername(email));
        securityUser.setEmail(email);
        securityUser.setPasswordHash(null);
        securityUser.setRole(DEFAULT_ROLE);
        securityUser.setAuthProvider(AuthProvider.GOOGLE);
        securityUser.setActive(true);
        securityUser.setMustResetPassword(false);
        return securityUserRepository.save(securityUser);
    }

    private UserDto createUserProfile(String email, GoogleIdToken.Payload payload) {
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        return usersClient.create(
                deriveUsername(email),
                firstName != null ? firstName : email,
                lastName != null ? lastName : "",
                email,
                userTypeCache.getDefaultNonAdminUserTypeId());
    }

    /**
     * Google-provisioned users never log in with a username/password (id-token
     * passthrough only), so this only needs to satisfy the NOT NULL/UNIQUE
     * constraint shared with LOCAL accounts — collisions are not retried.
     */
    private String deriveUsername(String email) {
        int at = email.indexOf('@');
        return (at > 0 ? email.substring(0, at) : email).toLowerCase();
    }

    private GoogleIdToken.Payload verify(String rawIdToken, String ipAddress) {
        try {
            GoogleIdToken idToken = verifier.verify(rawIdToken);
            if (idToken == null) {
                auditEventPublisher.publishLoginFailed(null, ipAddress);
                throw new InvalidGoogleTokenException("signature or audience check failed");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | java.io.IOException | IllegalArgumentException e) {
            auditEventPublisher.publishLoginFailed(null, ipAddress);
            throw new InvalidGoogleTokenException(e.getMessage());
        }
    }
}
