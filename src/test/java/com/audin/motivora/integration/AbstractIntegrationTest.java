package com.audin.motivora.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.DockerClientFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base for the HTTP contract tests.
 *
 * Runs against a real PostgreSQL so Flyway and {@code ddl-auto: validate} are exercised —
 * an in-memory database would let a migration/entity mismatch slip through, which is exactly
 * the failure that breaks a deployment.
 *
 * The container is started once for the whole suite and reused across test classes. Where
 * no Docker daemon is reachable the tests are skipped rather than failed, so a contributor
 * without Docker can still run the unit suite; CI always has one.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@EnabledIf("dockerAvailable")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Probes the daemon for real rather than trusting {@code isDockerAvailable()}, which
     * reports true on a host whose Docker API version the client cannot negotiate — the
     * container then fails to start and every test in the class errors instead of skipping.
     */
    static boolean dockerAvailable() {
        try {
            DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", PostgresContainer.INSTANCE::getPassword);
    }
}
