package com.vaadinbaseapp.msusers.mapper;

import com.vaadinbaseapp.msusers.dto.UserResponse;
import com.vaadinbaseapp.msusers.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final UserTypeMapper userTypeMapper;

    public UserMapper(UserTypeMapper userTypeMapper) {
        this.userTypeMapper = userTypeMapper;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),
                user.getCreatedAt(),
                userTypeMapper.toResponse(user.getUserType())
        );
    }
}
