package com.rzodeczko;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class DeadLetterTopicIntegrationTest extends AbstractIntegrationTest {

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokerAddresses;

    @Test
    void shouldRouteUndeserializableAvailabilityMessageToDlt() {
        // Producer sending raw garbage bytes (not valid Avro)
        KafkaTemplate<String, byte[]> poisonProducer = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
                ))
        );

        // Consumer reading from the DLT topic
        KafkaConsumer<String, byte[]> dltConsumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses,
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class
        ));
        dltConsumer.subscribe(List.of("travel.availability.DLT"));

        // Send poison pill — invalid bytes that cannot be deserialized as Avro
        byte[] garbage = "not-avro".getBytes();
        poisonProducer.send(new ProducerRecord<>("travel.availability", "poison-key", garbage));

        // Verify the message lands in the DLT
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            ConsumerRecords<String, byte[]> records = dltConsumer.poll(Duration.ofMillis(500));
            assertThat(records.count()).isGreaterThan(0);

            var record = records.iterator().next();
            assertThat(record.topic()).isEqualTo("travel.availability.DLT");
            assertThat(record.key()).isEqualTo("poison-key");
            assertThat(record.value()).isEqualTo(garbage);
        });

        dltConsumer.close();
    }

    @Test
    void shouldRouteUndeserializableHotelMessageToDlt() {
        KafkaTemplate<String, byte[]> poisonProducer = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class
                ))
        );

        KafkaConsumer<String, byte[]> dltConsumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses,
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-hotel-test-group",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class
        ));
        dltConsumer.subscribe(List.of("travel.hotels.DLT"));

        byte[] garbage = "broken-hotel-event".getBytes();
        poisonProducer.send(new ProducerRecord<>("travel.hotels", "hotel-poison", garbage));

        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(1)).untilAsserted(() -> {
            ConsumerRecords<String, byte[]> records = dltConsumer.poll(Duration.ofMillis(500));
            assertThat(records.count()).isGreaterThan(0);

            var record = records.iterator().next();
            assertThat(record.topic()).isEqualTo("travel.hotels.DLT");
            assertThat(record.key()).isEqualTo("hotel-poison");
            assertThat(record.value()).isEqualTo(garbage);
        });

        dltConsumer.close();
    }
}
