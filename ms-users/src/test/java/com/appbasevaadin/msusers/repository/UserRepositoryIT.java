package com.appbasevaadin.msusers.repository;

import com.appbasevaadin.msusers.entity.UserType;
import com.appbasevaadin.msusers.entity.User;
import com.appbasevaadin.msusers.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT extends PostgresTestContainerBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    private UserType adminUserType;

    @BeforeEach
    void setUp() {
        adminUserType = userTypeRepository.findAll().stream()
                .filter(t -> t.getName().equals("Administrator"))
                .findFirst()
                .orElseGet(() -> {
                    UserType type = new UserType();
                    type.setName("Administrator");
                    type.setDescription("Full access");
                    return userTypeRepository.save(type);
                });

        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");
        user.setUserType(adminUserType);
        userRepository.save(user);
    }

    @Test
    void searchWithTextFilterDoesNotThrowByteaError() {
        assertThatCode(() -> userRepository.search("jane", null, null, PageRequest.of(0, 10)))
                .doesNotThrowAnyException();

        Page<User> result = userRepository.search("jane", null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void searchWithNullTextReturnsAll() {
        Page<User> result = userRepository.search(null, null, null, PageRequest.of(0, 10));
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void findByIdLoadsUserTypeWithoutLazyException() {
        User saved = userRepository.findAll().get(0);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThatCode(() -> found.get().getUserType().getName())
                .doesNotThrowAnyException();
    }
}
