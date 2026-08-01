package com.vaadinbaseapp.msaudit.messaging;

import com.vaadinbaseapp.msaudit.repository.AuditEventRepository;
import com.vaadinbaseapp.msaudit.support.FullStackTestContainerBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuditEventListenerIT extends FullStackTestContainerBase {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    void aRealLoginFailedMessagePublishedToKafkaEndsUpPersisted() throws Exception {
        String email = "kafka.e2e." + System.nanoTime() + "@example.com";
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(
                new AuditEventMessage("LOGIN_FAILED", email, "203.0.113.9", LocalDateTime.now()));

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>("audit", email, json)).get();
        }

        Awaitility.await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(auditEventRepository.findAll())
                        .anyMatch(event -> email.equals(event.getEmail()) && "LOGIN_FAILED".equals(event.getType())));
    }
}
