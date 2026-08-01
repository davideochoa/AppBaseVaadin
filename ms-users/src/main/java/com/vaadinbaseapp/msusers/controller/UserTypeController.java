package com.vaadinbaseapp.msusers.controller;

import com.vaadinbaseapp.msusers.dto.UserTypeRequest;
import com.vaadinbaseapp.msusers.dto.UserTypeResponse;
import com.vaadinbaseapp.msusers.mapper.UserTypeMapper;
import com.vaadinbaseapp.msusers.service.UserTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user-types")
public class UserTypeController {

    private final UserTypeService userTypeService;
    private final UserTypeMapper userTypeMapper;

    public UserTypeController(UserTypeService userTypeService, UserTypeMapper userTypeMapper) {
        this.userTypeService = userTypeService;
        this.userTypeMapper = userTypeMapper;
    }

    @GetMapping
    public List<UserTypeResponse> list() {
        return userTypeService.list().stream()
                .map(userTypeMapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<UserTypeResponse> create(@Valid @RequestBody UserTypeRequest request) {
        UserTypeResponse response = userTypeMapper.toResponse(userTypeService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public UserTypeResponse update(@PathVariable Long id, @Valid @RequestBody UserTypeRequest request) {
        return userTypeMapper.toResponse(userTypeService.update(id, request));
    }
}
