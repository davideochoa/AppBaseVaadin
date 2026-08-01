package com.vaadinbaseapp.appvaadin.facade;

import com.vaadinbaseapp.appvaadin.client.UsersApiClient;
import com.vaadinbaseapp.appvaadin.dto.PageResponse;
import com.vaadinbaseapp.appvaadin.dto.UserRequest;
import com.vaadinbaseapp.appvaadin.dto.UserResponse;
import com.vaadinbaseapp.appvaadin.dto.UserTypeRequest;
import com.vaadinbaseapp.appvaadin.dto.UserTypeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserFacade {

    private final UsersApiClient usersApiClient;

    public UserFacade(UsersApiClient usersApiClient) {
        this.usersApiClient = usersApiClient;
    }

    public PageResponse<UserResponse> search(String text, Long userTypeId, Boolean active, int page, int size) {
        return usersApiClient.search(text, userTypeId, active, page, size);
    }

    public UserResponse getById(Long id) {
        return usersApiClient.getById(id);
    }

    public UserResponse getByEmail(String email) {
        return usersApiClient.getByEmail(email);
    }

    public UserResponse create(UserRequest request) {
        return usersApiClient.create(request);
    }

    public UserResponse update(Long id, UserRequest request) {
        return usersApiClient.update(id, request);
    }

    public void hardDelete(Long id) {
        usersApiClient.hardDelete(id);
    }

    public List<UserTypeResponse> listUserTypes() {
        return usersApiClient.listUserTypes();
    }

    public UserTypeResponse createUserType(UserTypeRequest request) {
        return usersApiClient.createUserType(request);
    }

    public UserTypeResponse updateUserType(Long id, UserTypeRequest request) {
        return usersApiClient.updateUserType(id, request);
    }
}
