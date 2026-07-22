package com.appbasevaadin.msaudit.repository;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import com.appbasevaadin.msaudit.support.PostgresTestContainerBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditEventRepositoryIT extends PostgresTestContainerBase {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @BeforeEach
    void setUp() {
        AuditEvent event = new AuditEvent();
        event.setType("LOGIN_FAILED");
        event.setEmail("jane.doe@example.com");
        event.setIpAddress("203.0.113.7");
        event.setOccurredAt(LocalDateTime.now());
        auditEventRepository.save(event);
    }

    @Test
    void searchWithTypeFilterDoesNotThrowByteaError() {
        assertThatCode(() -> auditEventRepository.search("LOGIN_FAILED", null, PageRequest.of(0, 10)))
                .doesNotThrowAnyException();

        Page<AuditEvent> result = auditEventRepository.search("LOGIN_FAILED", null, PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchWithEmailFilterIsCaseInsensitive() {
        Page<AuditEvent> result = auditEventRepository.search(null, "JANE.DOE@EXAMPLE.COM", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIpAddress()).isEqualTo("203.0.113.7");
    }

    @Test
    void searchWithNoFiltersReturnsAll() {
        Page<AuditEvent> result = auditEventRepository.search(null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotEmpty();
    }
}
