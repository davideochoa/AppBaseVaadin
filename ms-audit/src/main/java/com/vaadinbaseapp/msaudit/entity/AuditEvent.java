package com.vaadinbaseapp.msaudit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_event")
@Getter
@Setter
@NoArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, columnDefinition = "varchar(50)")
    private String type;

    @Column(name = "email", columnDefinition = "varchar(150)")
    private String email;

    @Column(name = "ip_address", columnDefinition = "varchar(45)")
    private String ipAddress;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @jakarta.persistence.PrePersist
    void onCreate() {
        this.receivedAt = LocalDateTime.now();
    }
}
