package com.appbasevaadin.msusers.service;

import com.appbasevaadin.msusers.dto.UserTypeRequest;
import com.appbasevaadin.msusers.entity.UserType;
import com.appbasevaadin.msusers.exception.UserTypeNotFoundException;
import com.appbasevaadin.msusers.repository.UserTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserTypeService {

    private final UserTypeRepository userTypeRepository;

    public UserTypeService(UserTypeRepository userTypeRepository) {
        this.userTypeRepository = userTypeRepository;
    }

    public List<UserType> list() {
        return userTypeRepository.findAll();
    }

    @Transactional
    public UserType create(UserTypeRequest request) {
        UserType userType = new UserType();
        applyData(userType, request);
        userType.setActive(request.getActive() == null || request.getActive());
        return userTypeRepository.save(userType);
    }

    @Transactional
    public UserType update(Long id, UserTypeRequest request) {
        UserType userType = userTypeRepository.findById(id)
                .orElseThrow(() -> new UserTypeNotFoundException(id));
        applyData(userType, request);
        if (request.getActive() != null) {
            userType.setActive(request.getActive());
        }
        return userTypeRepository.save(userType);
    }

    private void applyData(UserType userType, UserTypeRequest request) {
        userType.setName(request.getName());
        userType.setDescription(request.getDescription());
    }
}
