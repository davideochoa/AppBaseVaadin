package com.appbasevaadin.appvaadin.facade;

import com.appbasevaadin.appvaadin.client.UsersApiClient;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.dto.UserRequest;
import com.appbasevaadin.appvaadin.dto.UserResponse;
import com.appbasevaadin.appvaadin.dto.UserTypeResponse;
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

    public UserResponse create(UserRequest request) {
        return usersApiClient.create(request);
    }

    public UserResponse update(Long id, UserRequest request) {
        return usersApiClient.update(id, request);
    }

    public void delete(Long id) {
        usersApiClient.delete(id);
    }

    public List<UserTypeResponse> listUserTypes() {
        return usersApiClient.listUserTypes();
    }
}
