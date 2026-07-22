package com.appbasevaadin.mssecurity.service;

import com.appbasevaadin.mssecurity.client.UsersClient;
import com.appbasevaadin.mssecurity.client.dto.UserDto;
import com.appbasevaadin.mssecurity.dto.TokenResponse;
import com.appbasevaadin.mssecurity.entity.AuthProvider;
import com.appbasevaadin.mssecurity.entity.SecurityUser;
import com.appbasevaadin.mssecurity.exception.InvalidGoogleTokenException;
import com.appbasevaadin.mssecurity.messaging.AuditEventPublisher;
import com.appbasevaadin.mssecurity.repository.SecurityUserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleLoginService {

    private static final String DEFAULT_ROLE = "USER";

    private final GoogleIdTokenVerifier verifier;
    private final SecurityUserRepository securityUserRepository;
    private final UsersClient usersClient;
    private final UserTypeCache userTypeCache;
    private final AuthService authService;
    private final AuditEventPublisher auditEventPublisher;

    public GoogleLoginService(@Value("${app.google.client-id}") String googleClientId,
                               SecurityUserRepository securityUserRepository,
                               UsersClient usersClient,
                               UserTypeCache userTypeCache,
                               AuthService authService,
                               AuditEventPublisher auditEventPublisher) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
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
        securityUser.setEmail(email);
        securityUser.setPasswordHash(null);
        securityUser.setRole(DEFAULT_ROLE);
        securityUser.setAuthProvider(AuthProvider.GOOGLE);
        securityUser.setActive(true);
        return securityUserRepository.save(securityUser);
    }

    private UserDto createUserProfile(String email, GoogleIdToken.Payload payload) {
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        return usersClient.create(
                firstName != null ? firstName : email,
                lastName != null ? lastName : "",
                email,
                userTypeCache.getDefaultNonAdminUserTypeId());
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
