package com.appbasevaadin.msusers.mapper;

import com.appbasevaadin.msusers.dto.UserTypeResponse;
import com.appbasevaadin.msusers.entity.UserType;
import org.springframework.stereotype.Component;

@Component
public class UserTypeMapper {

    public UserTypeResponse toResponse(UserType userType) {
        return new UserTypeResponse(userType.getId(), userType.getName(), userType.getDescription(),
                userType.isActive());
    }
}
