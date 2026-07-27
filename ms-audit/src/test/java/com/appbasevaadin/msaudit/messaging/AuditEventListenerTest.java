package com.appbasevaadin.msaudit.messaging;

import com.appbasevaadin.msaudit.entity.AuditEvent;
import com.appbasevaadin.msaudit.mapper.AuditEventMapper;
import com.appbasevaadin.msaudit.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Test
    void onMessagePersistsAnAuditEventWithTheSameFields() {
        AuditEventListener listener = new AuditEventListener(auditEventRepository, new AuditEventMapper());
        LocalDateTime occurredAt = LocalDateTime.now();
        AuditEventMessage message = new AuditEventMessage("LOGIN_FAILED", "jane.doe@example.com",
                "203.0.113.7", occurredAt);

        listener.onMessage(message);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        AuditEvent saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo("LOGIN_FAILED");
        assertThat(saved.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(saved.getIpAddress()).isEqualTo("203.0.113.7");
        assertThat(saved.getOccurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void onMessageWithNullEmailIsPersistedAsIs() {
        AuditEventListener listener = new AuditEventListener(auditEventRepository, new AuditEventMapper());
        AuditEventMessage message = new AuditEventMessage("LOGIN_FAILED", null, "203.0.113.7", LocalDateTime.now());

        listener.onMessage(message);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isNull();
    }
}
