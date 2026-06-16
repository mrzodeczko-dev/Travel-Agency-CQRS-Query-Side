package com.rzodeczko;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        createTopics();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri",
                () -> mongoDBContainer.getConnectionString() + "/test");
        registry.add("spring.kafka.bootstrap-servers",
                kafkaContainer::getBootstrapServers);
    }

    private static void createTopics() {
        try (AdminClient admin = AdminClient.create(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()))) {
            List<NewTopic> newTopics = Arrays.stream(new String[]{
                            "travel.bookings", "travel.availability", "travel.availability.DLT", "travel.hotels", "travel.hotels.DLT"})
                    .map(t -> new NewTopic(t, 1, (short) 1))
                    .toList();
            admin.createTopics(newTopics).all().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Kafka topics for integration tests", e);
        }
    }
}
