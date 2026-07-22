package com.appbasevaadin.msusers.service;

import com.appbasevaadin.msusers.dto.UserRequest;
import com.appbasevaadin.msusers.entity.UserType;
import com.appbasevaadin.msusers.entity.User;
import com.appbasevaadin.msusers.exception.UserTypeNotFoundException;
import com.appbasevaadin.msusers.exception.UserNotFoundException;
import com.appbasevaadin.msusers.repository.UserTypeRepository;
import com.appbasevaadin.msusers.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTypeRepository userTypeRepository;

    private UserService userService;

    private UserType userType;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userTypeRepository);
        userType = new UserType();
        userType.setId(1L);
        userType.setName("User");
    }

    @Test
    void createSavesActiveUserByDefault() {
        UserRequest request = new UserRequest();
        request.setFirstName("Luis");
        request.setLastName("Perez");
        request.setEmail("luis.perez@example.com");
        request.setUserTypeId(1L);

        when(userTypeRepository.findById(1L)).thenReturn(Optional.of(userType));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User created = userService.create(request);

        assertThat(created.isActive()).isTrue();
        assertThat(created.getEmail()).isEqualTo("luis.perez@example.com");
        assertThat(created.getUserType()).isEqualTo(userType);
    }

    @Test
    void createWithNonexistentUserTypeThrowsException() {
        UserRequest request = new UserRequest();
        request.setFirstName("Luis");
        request.setLastName("Perez");
        request.setEmail("luis.perez@example.com");
        request.setUserTypeId(99L);

        when(userTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(UserTypeNotFoundException.class);
    }

    @Test
    void getByIdNonexistentThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void deleteMarksUserAsInactive() {
        User user = new User();
        user.setId(5L);
        user.setActive(true);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.delete(5L);

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }
}
