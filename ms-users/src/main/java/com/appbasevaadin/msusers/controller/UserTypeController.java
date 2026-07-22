package com.appbasevaadin.msusers.controller;

import com.appbasevaadin.msusers.dto.UserTypeResponse;
import com.appbasevaadin.msusers.service.UserTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user-types")
public class UserTypeController {

    private final UserTypeService userTypeService;

    public UserTypeController(UserTypeService userTypeService) {
        this.userTypeService = userTypeService;
    }

    @GetMapping
    public List<UserTypeResponse> list() {
        return userTypeService.list().stream()
                .map(UserTypeResponse::from)
                .toList();
    }
}
