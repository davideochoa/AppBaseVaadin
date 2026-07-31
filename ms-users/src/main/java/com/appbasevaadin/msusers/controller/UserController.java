package com.appbasevaadin.msusers.controller;

import com.appbasevaadin.msusers.dto.UserRequest;
import com.appbasevaadin.msusers.dto.UserResponse;
import com.appbasevaadin.msusers.mapper.UserMapper;
import com.appbasevaadin.msusers.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'SERVICE')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userMapper.toResponse(userService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userMapper.toResponse(userService.getById(id));
    }

    @GetMapping("/by-email")
    public UserResponse getByEmail(@RequestParam String email) {
        return userMapper.toResponse(userService.getByEmail(email));
    }

    @GetMapping
    public Page<UserResponse> search(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) Long userTypeId,
                                      @RequestParam(required = false) Boolean active,
                                      Pageable pageable) {
        return userService.search(text, userTypeId, active, pageable)
                .map(userMapper::toResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userMapper.toResponse(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) {
        userService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
