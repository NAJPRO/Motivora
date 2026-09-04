package com.audin.motivora.integration;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Holds the shared PostgreSQL container.
 *
 * Kept in its own class so that merely evaluating the "is Docker available?" condition on
 * {@link AbstractIntegrationTest} does not start a container: the class is only initialised
 * when a test that needs the database actually runs.
 */
final class PostgresContainer {

    static final PostgreSQLContainer<?> INSTANCE = new PostgreSQLContainer<>("postgres:14.19-alpine3.21")
            .withDatabaseName("motivora_test")
            .withUsername("motivora")
            .withPassword("motivora");

    static {
        INSTANCE.start();
    }

    private PostgresContainer() {
    }
}
