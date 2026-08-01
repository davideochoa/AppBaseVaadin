package com.vaadinbaseapp.msusers.mapper;

import com.vaadinbaseapp.msusers.dto.UserTypeResponse;
import com.vaadinbaseapp.msusers.entity.UserType;
import org.springframework.stereotype.Component;

@Component
public class UserTypeMapper {

    public UserTypeResponse toResponse(UserType userType) {
        return new UserTypeResponse(userType.getId(), userType.getName(), userType.getDescription(),
                userType.isActive());
    }
}
