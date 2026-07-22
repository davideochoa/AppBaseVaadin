package com.appbasevaadin.msusers.service;

import com.appbasevaadin.msusers.entity.UserType;
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
}
