package com.appbasevaadin.appvaadin.facade;

import com.appbasevaadin.appvaadin.client.ApiException;
import com.appbasevaadin.appvaadin.dto.ApiError;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import com.appbasevaadin.appvaadin.dto.UserRequest;
import com.appbasevaadin.appvaadin.dto.UserResponse;
import com.appbasevaadin.appvaadin.dto.UserTypeResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stands in for the real UsersApiClient + ms-users backend: everything lives in an in-memory
 * list for the lifetime of this JVM, no REST call ever leaves the process. Kept behind the same
 * facade signature as the real app-vaadin so the views above it are untouched.
 */
@Component
public class UserFacade {

    private static final List<UserTypeResponse> USER_TYPES = List.of(
            new UserTypeResponse(1L, "Administrator", "Full access to manage users"),
            new UserTypeResponse(2L, "User", "Standard, non-administrative access"));

    private final List<UserResponse> users = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong();

    public UserFacade() {
        seed();
    }

    private void seed() {
        create(new UserRequest("Ada", "Lovelace", "ada.lovelace@example.com", 1L, true));
        create(new UserRequest("Grace", "Hopper", "grace.hopper@example.com", 2L, true));
        create(new UserRequest("Alan", "Turing", "alan.turing@example.com", 2L, true));
        create(new UserRequest("Margaret", "Hamilton", "margaret.hamilton@example.com", 2L, false));
    }

    public synchronized PageResponse<UserResponse> search(String text, Long userTypeId, Boolean active, int page,
                                                            int size) {
        String normalizedText = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        List<UserResponse> filtered = users.stream()
                .filter(u -> normalizedText.isBlank()
                        || u.firstName().toLowerCase(Locale.ROOT).contains(normalizedText)
                        || u.lastName().toLowerCase(Locale.ROOT).contains(normalizedText)
                        || u.email().toLowerCase(Locale.ROOT).contains(normalizedText))
                .filter(u -> userTypeId == null || userTypeId.equals(u.userType().id()))
                .filter(u -> active == null || active.equals(u.active()))
                .sorted(Comparator.comparing(UserResponse::id))
                .toList();

        return paginate(filtered, page, size);
    }

    public UserResponse getById(Long id) {
        return users.stream().filter(u -> u.id().equals(id)).findFirst()
                .orElseThrow(() -> new ApiException(notFound(id)));
    }

    public synchronized UserResponse create(UserRequest request) {
        validate(request);
        assertEmailAvailable(request.email(), null);
        UserResponse created = new UserResponse(idSequence.incrementAndGet(), request.firstName(), request.lastName(),
                request.email(), request.active() == null || request.active(), LocalDateTime.now(),
                resolveUserType(request.userTypeId()));
        users.add(created);
        return created;
    }

    public synchronized UserResponse update(Long id, UserRequest request) {
        validate(request);
        int index = indexOf(id);
        assertEmailAvailable(request.email(), id);
        UserResponse existing = users.get(index);
        UserResponse updated = new UserResponse(existing.id(), request.firstName(), request.lastName(),
                request.email(), request.active() == null ? existing.active() : request.active(),
                existing.createdAt(), resolveUserType(request.userTypeId()));
        users.set(index, updated);
        return updated;
    }

    public synchronized void delete(Long id) {
        int index = indexOf(id);
        UserResponse existing = users.get(index);
        users.set(index, new UserResponse(existing.id(), existing.firstName(), existing.lastName(),
                existing.email(), false, existing.createdAt(), existing.userType()));
    }

    public List<UserTypeResponse> listUserTypes() {
        return USER_TYPES;
    }

    private PageResponse<UserResponse> paginate(List<UserResponse> filtered, int page, int size) {
        int totalElements = filtered.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), totalElements, totalPages, page, size);
    }

    private int indexOf(Long id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new ApiException(notFound(id));
    }

    private UserTypeResponse resolveUserType(Long userTypeId) {
        return USER_TYPES.stream().filter(t -> t.id().equals(userTypeId)).findFirst()
                .orElse(USER_TYPES.get(USER_TYPES.size() - 1));
    }

    private void assertEmailAvailable(String email, Long excludingId) {
        boolean duplicate = users.stream()
                .filter(u -> excludingId == null || !u.id().equals(excludingId))
                .anyMatch(u -> u.email().equalsIgnoreCase(email));
        if (duplicate) {
            throw new ApiException(new ApiError(LocalDateTime.now(), 409, "DATA_CONFLICT",
                    "The resource already exists or violates a data constraint", List.of()));
        }
    }

    private void validate(UserRequest request) {
        List<ApiError.FieldError> errors = new ArrayList<>();
        if (isBlank(request.firstName())) {
            errors.add(new ApiError.FieldError("firstName", "must not be blank"));
        }
        if (isBlank(request.lastName())) {
            errors.add(new ApiError.FieldError("lastName", "must not be blank"));
        }
        if (isBlank(request.email()) || !request.email().contains("@")) {
            errors.add(new ApiError.FieldError("email", "must be a well-formed email address"));
        }
        if (!errors.isEmpty()) {
            throw new ApiException(new ApiError(LocalDateTime.now(), 400, "VALIDATION", "Validation failed", errors));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ApiError notFound(Long id) {
        return new ApiError(LocalDateTime.now(), 404, "NOT_FOUND", "User " + id + " not found", List.of());
    }
}
