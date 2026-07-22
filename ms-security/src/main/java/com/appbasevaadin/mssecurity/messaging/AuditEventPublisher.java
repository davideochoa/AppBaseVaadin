package com.appbasevaadin.mssecurity.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuditEventPublisher.class);

    private final KafkaTemplate<String, LoginFailedEvent> kafkaTemplate;
    private final String auditTopic;

    public AuditEventPublisher(KafkaTemplate<String, LoginFailedEvent> kafkaTemplate,
                                @Value("${app.kafka.audit-topic}") String auditTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.auditTopic = auditTopic;
    }

    /**
     * Best-effort: a Kafka outage must never break the caller's own error handling
     * (a failed login must still return its normal 401 either way).
     */
    public void publishLoginFailed(String email, String ipAddress) {
        try {
            kafkaTemplate.send(auditTopic, email, LoginFailedEvent.now(email, ipAddress))
                    .exceptionally(ex -> {
                        log.warn("Failed to publish LOGIN_FAILED audit event", ex);
                        return null;
                    });
        } catch (Exception ex) {
            log.warn("Failed to publish LOGIN_FAILED audit event", ex);
        }
    }
}
