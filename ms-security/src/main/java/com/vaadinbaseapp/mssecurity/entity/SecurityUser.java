package com.vaadinbaseapp.mssecurity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_security")
@Getter
@Setter
@NoArgsConstructor
public class SecurityUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, columnDefinition = "varchar(50)")
    private String username;

    @Column(name = "email", nullable = false, unique = true, columnDefinition = "varchar(150)")
    private String email;

    @Column(name = "password_hash", columnDefinition = "varchar(255)")
    private String passwordHash;

    @Column(name = "role", nullable = false, columnDefinition = "varchar(50)")
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, columnDefinition = "varchar(20)")
    private AuthProvider authProvider;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "must_reset_password", nullable = false)
    private boolean mustResetPassword = false;
}
