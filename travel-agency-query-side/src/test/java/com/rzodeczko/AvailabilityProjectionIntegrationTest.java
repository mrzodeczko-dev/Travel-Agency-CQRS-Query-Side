package com.rzodeczko;

import com.rzodeczko.avro.AvailabilityUpdatedAvro;
import com.rzodeczko.avro.BookingEventAvro;
import com.rzodeczko.avro.EventType;
import com.rzodeczko.avro.HotelUpsertedAvro;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import com.rzodeczko.infrastructure.persistence.repository.MongoDailyAvailabilityRepository;
import com.rzodeczko.infrastructure.persistence.repository.MongoHotelRepository;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class AvailabilityProjectionIntegrationTest extends AbstractIntegrationTest {

    @Value("${spring.kafka.bootstrap-servers}")
    private String brokerAddresses;

    @Autowired
    private MongoDailyAvailabilityRepository availabilityRepository;

    @Autowired
    private MongoHotelRepository hotelRepository;

    private KafkaTemplate<String, Object> producer;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddresses);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", MOCK_REGISTRY);

        producer = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));

        availabilityRepository.deleteAll();
        hotelRepository.deleteAll();
    }


    @Test
    void shouldUpsertAvailabilityDocumentWhenAvailabilityUpdatedEventArrives() {
        AvailabilityUpdatedAvro event = AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(1L)
                .setDate("2024-06-01")
                .setOccupied(3L)
                .build();

        producer.send("travel.availability", "1", event);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<AvailabilityDocument> doc = availabilityRepository.findById(
                    AvailabilityDocument.buildId(1L, LocalDate.of(2024, 6, 1)));

            assertThat(doc).isPresent();
            assertThat(doc.get().getHotelId()).isEqualTo(1L);
            assertThat(doc.get().getDate()).isEqualTo(LocalDate.of(2024, 6, 1));
            assertThat(doc.get().getOccupied()).isEqualTo(3L);
            assertThat(doc.get().getUpdatedAt()).isNotNull();
        });
    }

    @Test
    void shouldOverwriteExistingDocumentOnSubsequentAvailabilityUpdatedEvent() {
        // first event
        producer.send("travel.availability", "1", AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(1L).setDate("2024-06-02").setOccupied(2L).build());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(availabilityRepository.findById(
                        AvailabilityDocument.buildId(1L, LocalDate.of(2024, 6, 2)))).isPresent()
        );

        // updated count arrives
        producer.send("travel.availability", "1", AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(1L).setDate("2024-06-02").setOccupied(5L).build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<AvailabilityDocument> doc = availabilityRepository.findById(
                    AvailabilityDocument.buildId(1L, LocalDate.of(2024, 6, 2)));
            assertThat(doc).isPresent();
            assertThat(doc.get().getOccupied()).isEqualTo(5L);
        });
    }


    @Test
    void shouldPersistHotelDocumentWhenHotelUpsertedEventArrives() {
        producer.send("travel.hotels", "2", HotelUpsertedAvro.newBuilder()
                .setHotelId(2L)
                .setCapacity(120L)
                .build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<?> hotel = hotelRepository.findById(2L);
            assertThat(hotel).isPresent();
        });
    }

    @Test
    void shouldUseHotelCapacityFromMongoWhenCalculatingAvailabilityStatus() {
        // 1. Store hotel capacity via listener
        producer.send("travel.hotels", "3", HotelUpsertedAvro.newBuilder()
                .setHotelId(3L).setCapacity(10L).build());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(hotelRepository.findById(3L)).isPresent());

        // 2. Produce availability event with 9 out of 10 rooms occupied -> LAST_ROOMS (threshold 0.9)
        producer.send("travel.availability", "3", AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(3L).setDate("2024-06-10").setOccupied(9L).build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<AvailabilityDocument> doc = availabilityRepository.findById(
                    AvailabilityDocument.buildId(3L, LocalDate.of(2024, 6, 10)));

            assertThat(doc).isPresent();
            assertThat(doc.get().getCapacity()).isEqualTo(10L);
            assertThat(doc.get().getOccupied()).isEqualTo(9L);
            assertThat(doc.get().getStatus()).isEqualTo(AvailabilityStatus.LAST_ROOMS);
        });
    }

    @Test
    void shouldSetSoldOutStatusWhenAllRoomsOccupied() {
        producer.send("travel.hotels", "4", HotelUpsertedAvro.newBuilder()
                .setHotelId(4L).setCapacity(10L).build());

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(hotelRepository.findById(4L)).isPresent());

        producer.send("travel.availability", "4", AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(4L).setDate("2024-06-10").setOccupied(10L).build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<AvailabilityDocument> doc = availabilityRepository.findById(
                    AvailabilityDocument.buildId(4L, LocalDate.of(2024, 6, 10)));

            assertThat(doc).isPresent();
            assertThat(doc.get().getStatus()).isEqualTo(AvailabilityStatus.SOLD_OUT);
        });
    }

    // Full pipeline: BookingCreated -> Kafka Streams → MongoDB
    @Test
    void shouldProjectBookingCreatedEventThroughKafkaStreamsToMongoDB() {
        final long hotelId = 99L;
        final String date = "2024-08-01";

        // Step 1: establish hotel capacity
        producer.send("travel.hotels", String.valueOf(hotelId), HotelUpsertedAvro.newBuilder()
                .setHotelId(hotelId).setCapacity(20L).build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(hotelRepository.findById(hotelId)).isPresent());

        // Step 2: booking arrives on travel.bookings
        producer.send("travel.bookings", "booking-99-1", BookingEventAvro.newBuilder()
                .setEventType(EventType.BookingCreated).setId(1L)
                .setHotelId(hotelId)
                .setUserId(100L)
                .setStart(date)
                .setEnd(date)   // single-night booking
                .build());

        // Step 3: wait for the full pipeline to complete
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Optional<AvailabilityDocument> doc = availabilityRepository.findById(
                    AvailabilityDocument.buildId(hotelId, LocalDate.of(2024, 8, 1)));

            assertThat(doc).isPresent();
            assertThat(doc.get().getHotelId()).isEqualTo(hotelId);
            assertThat(doc.get().getDate()).isEqualTo(LocalDate.of(2024, 8, 1));
            assertThat(doc.get().getOccupied()).isEqualTo(1L);
            assertThat(doc.get().getCapacity()).isEqualTo(20L);
            assertThat(doc.get().getStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
        });
    }

    @Test
    void shouldAggregateMultipleBookingsForSameDayThroughFullPipeline() {
        final long hotelId = 98L;
        final String date = "2024-09-15";

        producer.send("travel.hotels", String.valueOf(hotelId), HotelUpsertedAvro.newBuilder()
                .setHotelId(hotelId).setCapacity(5L).build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(hotelRepository.findById(hotelId)).isPresent());

        producer.send("travel.bookings", "booking1", BookingEventAvro.newBuilder()
                .setEventType(EventType.BookingCreated).setId(1L).setHotelId(hotelId).setUserId(101L).setStart(date).setEnd(date).build());
        producer.send("travel.bookings", "booking2", BookingEventAvro.newBuilder()
                .setEventType(EventType.BookingCreated).setId(2L).setHotelId(hotelId).setUserId(102L).setStart(date).setEnd(date).build());
        producer.send("travel.bookings", "booking3", BookingEventAvro.newBuilder()
                .setEventType(EventType.BookingCreated).setId(3L).setHotelId(hotelId).setUserId(103L).setStart(date).setEnd(date).build());
        producer.send("travel.bookings", "booking4", BookingEventAvro.newBuilder()
                .setEventType(EventType.BookingCreated).setId(4L).setHotelId(hotelId).setUserId(104L).setStart(date).setEnd(date).build());
        producer.send("travel.bookings", "booking5", BookingEventAvro.newBuilder()
                .setEventType(EventType.BookingCreated).setId(5L).setHotelId(hotelId).setUserId(105L).setStart(date).setEnd(date).build());

        // 5 rooms booked out of 5 -> SOLD_OUT
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
            Optional<AvailabilityDocument> doc = availabilityRepository.findById(
                    AvailabilityDocument.buildId(hotelId, LocalDate.of(2024, 9, 15)));

            assertThat(doc).isPresent();
            assertThat(doc.get().getOccupied()).isEqualTo(5L);
            assertThat(doc.get().getStatus()).isEqualTo(AvailabilityStatus.SOLD_OUT);
        });
    }


    @Test
    void shouldReprojectAvailabilityStatusWhenHotelCapacityChanges() {
        final long hotelId = 77L;
        final String dateA = "2024-07-01";
        final String dateB = "2024-07-02";

        // Step 1: hotel with large capacity
        producer.send("travel.hotels", String.valueOf(hotelId), HotelUpsertedAvro.newBuilder()
                .setHotelId(hotelId).setCapacity(100L).build());

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(hotelRepository.findById(hotelId)).isPresent());

        // Step 2: produce availability events (simulating Streams output) for two dates
        producer.send("travel.availability", String.valueOf(hotelId), AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(hotelId).setDate(dateA).setOccupied(9L).build());
        producer.send("travel.availability", String.valueOf(hotelId), AvailabilityUpdatedAvro.newBuilder()
                .setHotelId(hotelId).setDate(dateB).setOccupied(10L).build());

        // Wait for both days to be persisted with AVAILABLE status (9/100 and 10/100)
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<AvailabilityDocument> docA = availabilityRepository.findById(
                    AvailabilityDocument.buildId(hotelId, LocalDate.of(2024, 7, 1)));
            Optional<AvailabilityDocument> docB = availabilityRepository.findById(
                    AvailabilityDocument.buildId(hotelId, LocalDate.of(2024, 7, 2)));

            assertThat(docA).isPresent();
            assertThat(docA.get().getStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
            assertThat(docB).isPresent();
            assertThat(docB.get().getStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
        });

        // Step 3: capacity drops from 100 → 10 → triggers reprojectHotelDays
        producer.send("travel.hotels", String.valueOf(hotelId), HotelUpsertedAvro.newBuilder()
                .setHotelId(hotelId).setCapacity(10L).build());

        // Step 4: verify reprojection — 9/10 = LAST_ROOMS, 10/10 = SOLD_OUT
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Optional<AvailabilityDocument> docA = availabilityRepository.findById(
                    AvailabilityDocument.buildId(hotelId, LocalDate.of(2024, 7, 1)));
            Optional<AvailabilityDocument> docB = availabilityRepository.findById(
                    AvailabilityDocument.buildId(hotelId, LocalDate.of(2024, 7, 2)));

            assertThat(docA).isPresent();
            assertThat(docA.get().getCapacity()).isEqualTo(10L);
            assertThat(docA.get().getOccupied()).isEqualTo(9L);
            assertThat(docA.get().getStatus()).isEqualTo(AvailabilityStatus.LAST_ROOMS);

            assertThat(docB).isPresent();
            assertThat(docB.get().getCapacity()).isEqualTo(10L);
            assertThat(docB.get().getOccupied()).isEqualTo(10L);
            assertThat(docB.get().getStatus()).isEqualTo(AvailabilityStatus.SOLD_OUT);
        });
    }
}
