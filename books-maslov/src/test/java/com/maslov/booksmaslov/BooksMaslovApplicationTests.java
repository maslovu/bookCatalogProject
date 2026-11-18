package com.maslov.booksmaslov;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "spring.liquibase.enabled=false",
    "spring.data.redis.enabled=false",
    "spring.kafka.enabled=false",
    "spring.ai.ollama.enabled=false",
    "spring.ai.vectorstore.pgvector.initialize-schema=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.hikari.max-lifetime=30000"
})
@Testcontainers
class BooksMaslovApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void contextLoads() {
    }
}
