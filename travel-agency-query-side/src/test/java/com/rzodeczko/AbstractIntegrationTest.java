package com.rzodeczko;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AbstractIntegrationTest {

    protected static final String MOCK_REGISTRY = "mock://integration-test";
    protected static final MongoDBContainer mongoDBContainer;
    protected static final ConfluentKafkaContainer kafkaContainer;

    static {
        mongoDBContainer = new MongoDBContainer("mongo:8.3.1");
        mongoDBContainer.start();

        kafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.0");
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri",
                () -> mongoDBContainer.getConnectionString() + "/test");
        registry.add("spring.kafka.bootstrap-servers",
                kafkaContainer::getBootstrapServers);
    }
}
