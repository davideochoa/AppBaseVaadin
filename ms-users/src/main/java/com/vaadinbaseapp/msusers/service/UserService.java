package com.vaadinbaseapp.msusers.service;

import com.vaadinbaseapp.msusers.dto.UserRequest;
import com.vaadinbaseapp.msusers.entity.User;
import com.vaadinbaseapp.msusers.entity.UserType;
import com.vaadinbaseapp.msusers.exception.UserNotFoundException;
import com.vaadinbaseapp.msusers.exception.UserTypeNotFoundException;
import com.vaadinbaseapp.msusers.repository.UserRepository;
import com.vaadinbaseapp.msusers.repository.UserTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;

    public UserService(UserRepository userRepository, UserTypeRepository userTypeRepository) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
    }

    public User create(UserRequest request) {
        User user = new User();
        applyData(user, request);
        user.setActive(request.getActive() == null || request.getActive());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> UserNotFoundException.forEmail(email));
    }

    @Transactional(readOnly = true)
    public Page<User> search(String text, Long userTypeId, Boolean active, Pageable pageable) {
        return userRepository.search(text, userTypeId, active, pageable);
    }

    public User update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        applyData(user, request);
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Permanent removal, unlike {@link #delete}. Only meant for a caller
     * compensating a just-created row (e.g. the security-user side of the
     * creation failed) — never for retiring a real, previously-used account,
     * which must go through the soft-delete above.
     */
    public void hardDelete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    private void applyData(User user, UserRequest request) {
        UserType userType = userTypeRepository.findById(request.getUserTypeId())
                .orElseThrow(() -> new UserTypeNotFoundException(request.getUserTypeId()));
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUserType(userType);
    }
}
