package com.appbasevaadin.mssecurity.repository;

import com.appbasevaadin.mssecurity.entity.AuthProvider;
import com.appbasevaadin.mssecurity.entity.SecurityUser;
import com.appbasevaadin.mssecurity.support.PostgresTestContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityUserRepositoryIT extends PostgresTestContainerBase {

    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Test
    void findByEmailIgnoreCaseFindsSeededOrInsertedUser() {
        SecurityUser user = new SecurityUser();
        user.setUserId(42L);
        user.setEmail("jane.doe@example.com");
        user.setPasswordHash("irrelevant-hash");
        user.setRole("USER");
        user.setAuthProvider(AuthProvider.LOCAL);
        securityUserRepository.save(user);

        Optional<SecurityUser> found = securityUserRepository.findByEmailIgnoreCase("JANE.DOE@EXAMPLE.COM");

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo("USER");
    }

    @Test
    void bootstrapAdminSeedIsPresent() {
        long count = securityUserRepository.findAll().stream()
                .filter(u -> u.getRole().equals("ADMINISTRATOR") && u.getAuthProvider() == AuthProvider.LOCAL)
                .count();

        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
