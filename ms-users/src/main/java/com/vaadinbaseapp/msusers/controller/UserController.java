package com.vaadinbaseapp.msusers.controller;

import com.vaadinbaseapp.msusers.dto.UserRequest;
import com.vaadinbaseapp.msusers.dto.UserResponse;
import com.vaadinbaseapp.msusers.mapper.UserMapper;
import com.vaadinbaseapp.msusers.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    /**
     * Unlike {@code GET /{id}}/{@code search} (public by design — no sensitive
     * data), this accepts a bare email as the lookup key, which is exactly the
     * shape of a user-enumeration primitive. Any authenticated caller may look
     * up their own profile this way (app-vaadin's MainLayout does, to render
     * the current user's display name), and ms-security's inter-service
     * SERVICE-token caller needs unrestricted lookup for Google
     * auto-provisioning — but a regular user looking up someone else's email
     * is not a legitimate case and is now rejected.
     */
    @GetMapping("/by-email")
    public UserResponse getByEmail(@RequestParam String email, @AuthenticationPrincipal Jwt jwt) {
        String callerRole = jwt.getClaimAsString("role");
        boolean isPrivilegedCaller = "ADMINISTRATOR".equals(callerRole) || "SERVICE".equals(callerRole);
        boolean isSelfLookup = email.equalsIgnoreCase(jwt.getClaimAsString("email"));
        if (!isPrivilegedCaller && !isSelfLookup) {
            throw new AccessDeniedException("Not authorized to look up another user's profile by email");
        }
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
