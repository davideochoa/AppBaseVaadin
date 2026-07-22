package com.appbasevaadin.msusers.dto;

import com.appbasevaadin.msusers.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserTypeResponse {

    private final Long id;
    private final String name;
    private final String description;

    public static UserTypeResponse from(UserType userType) {
        return new UserTypeResponse(userType.getId(), userType.getName(), userType.getDescription());
    }
}
