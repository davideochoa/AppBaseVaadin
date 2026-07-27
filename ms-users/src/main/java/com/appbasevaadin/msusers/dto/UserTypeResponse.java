package com.appbasevaadin.msusers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Plain response payload — mapping from {@code UserType} lives in
 * {@link com.appbasevaadin.msusers.mapper.UserTypeMapper}, not here, so this class stays a pure data holder.
 */
@Getter
@AllArgsConstructor
public class UserTypeResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final boolean active;
}
