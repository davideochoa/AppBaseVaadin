package com.vaadinbaseapp.mssecurity.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class PostgresTestContainerBase {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("ms_security_test")
            .withUsername("ms_security_app")
            .withPassword("ms_security_pass");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    // Note: the V3 bootstrap-admin Flyway migration reads BOOTSTRAP_ADMIN_EMAIL/PASSWORD via
    // System.getenv() directly (Flyway Java migrations aren't Spring-managed, so they can't see
    // @DynamicPropertySource values). Tests that need a known local-login account should insert
    // their own SecurityUser row instead of depending on that seeded one.
}
