package com.appbasevaadin.appvaadin.facade;

import com.appbasevaadin.appvaadin.dto.AuditEventResponse;
import com.appbasevaadin.appvaadin.dto.PageResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stands in for the real AuditApiClient + ms-audit backend: a fixed set of sample
 * LOGIN_FAILED-style events kept in memory, filtered/paginated the same way the real
 * ms-audit search endpoint would.
 */
@Component
public class AuditFacade {

    private final List<AuditEventResponse> events = new CopyOnWriteArrayList<>();
    private final AtomicLong idSequence = new AtomicLong();

    public AuditFacade() {
        seed();
    }

    private void seed() {
        add("LOGIN_FAILED", "unknown@example.com", "203.0.113.10", LocalDateTime.now().minusHours(3));
        add("LOGIN_FAILED", "ada.lovelace@example.com", "198.51.100.23", LocalDateTime.now().minusHours(1).minusMinutes(15));
        add("LOGIN_FAILED", "grace.hopper@example.com", "192.0.2.77", LocalDateTime.now().minusMinutes(20));
    }

    private void add(String type, String email, String ip, LocalDateTime occurredAt) {
        events.add(new AuditEventResponse(idSequence.incrementAndGet(), type, email, ip, occurredAt,
                occurredAt.plusSeconds(1)));
    }

    public PageResponse<AuditEventResponse> search(String type, String email, int page, int size) {
        String normalizedType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        List<AuditEventResponse> filtered = events.stream()
                .filter(e -> normalizedType.isBlank() || e.type().toLowerCase(Locale.ROOT).contains(normalizedType))
                .filter(e -> normalizedEmail.isBlank()
                        || (e.email() != null && e.email().toLowerCase(Locale.ROOT).contains(normalizedEmail)))
                .sorted(Comparator.comparing(AuditEventResponse::occurredAt).reversed())
                .toList();

        int totalElements = filtered.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(filtered.subList(fromIndex, toIndex), totalElements, totalPages, page, size);
    }
}
