package com.appbasevaadin.msusers.mapper;

import com.appbasevaadin.msusers.dto.UserResponse;
import com.appbasevaadin.msusers.entity.User;
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
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),
                user.getCreatedAt(),
                userTypeMapper.toResponse(user.getUserType())
        );
    }
}
