package com.rzodeczko;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
        partitions = 1,
        topics = {
                "travel.bookings",
                "travel.availability",
                "travel.availability.DLT",
                "travel.hotels",
                "travel.hotels.DLT"
        },
        brokerProperties = {
                "auto.create.topics.enable=true",           // Kafka Streams creates internal topics at runtime
                "transaction.state.log.min.isr=1",          // required for single-broker setup
                "transaction.state.log.replication.factor=1"
        }
)
public class AbstractIntegrationTest {

    protected static final String MOCK_REGISTRY = "mock://integration-test";
    protected static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer("mongo:8.3.1");
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri",
                () -> mongoDBContainer.getConnectionString() + "/test");
    }
}
