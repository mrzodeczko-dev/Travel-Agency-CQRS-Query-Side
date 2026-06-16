package com.rzodeczko.infrastructure.persistence.adapter;

import com.rzodeczko.application.dto.PagedResult;
import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import com.rzodeczko.infrastructure.persistence.mapper.AvailabilityDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoDailyAvailabilityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoAvailabilityAdapterTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private MongoDailyAvailabilityRepository repository;
    @Mock
    private AvailabilityDocumentMapper mapper;

    @InjectMocks
    private MongoAvailabilityAdapter adapter;

    private static final long HOTEL_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2024, 6, 1);

    // ── findByHotel ──────────────────────────────────────────────────────────

    @Test
    void shouldUseDateRangeQueryWhenBothDatesProvided() {
        LocalDate from = LocalDate.of(2024, 6, 1);
        LocalDate to = LocalDate.of(2024, 6, 7);

        AvailabilityDocument doc = AvailabilityDocument.builder()
                .hotelId(HOTEL_ID).date(from).occupied(10).capacity(100)
                .status(AvailabilityStatus.AVAILABLE).updatedAt(Instant.now()).build();

        Availability domain = new Availability(HOTEL_ID, from, 10, 100, AvailabilityStatus.AVAILABLE);

        when(repository.findByHotelIdAndDateBetweenOrderByDateAsc(HOTEL_ID, from, to))
                .thenReturn(List.of(doc));
        when(mapper.toDomain(doc)).thenReturn(domain);

        List<Availability> result = adapter.findByHotel(HOTEL_ID, from, to);

        assertThat(result).containsExactly(domain);
        verify(repository).findByHotelIdAndDateBetweenOrderByDateAsc(HOTEL_ID, from, to);
        verify(repository, never()).findByHotelIdOrderByDateAsc(HOTEL_ID);
    }

    @Test
    void shouldUseAllDaysQueryWhenDatesAreNull() {
        AvailabilityDocument doc = AvailabilityDocument.builder()
                .hotelId(HOTEL_ID).date(DATE).occupied(5).capacity(50)
                .status(AvailabilityStatus.AVAILABLE).updatedAt(Instant.now()).build();

        Availability domain = new Availability(HOTEL_ID, DATE, 5, 50, AvailabilityStatus.AVAILABLE);

        when(repository.findByHotelIdOrderByDateAsc(HOTEL_ID)).thenReturn(List.of(doc));
        when(mapper.toDomain(doc)).thenReturn(domain);

        List<Availability> result = adapter.findByHotel(HOTEL_ID, null, null);

        assertThat(result).containsExactly(domain);
        verify(repository).findByHotelIdOrderByDateAsc(HOTEL_ID);
    }

    @Test
    void shouldFallBackToAllDaysWhenOnlyFromIsProvided() {
        when(repository.findByHotelIdOrderByDateAsc(HOTEL_ID)).thenReturn(List.of());

        List<Availability> result = adapter.findByHotel(HOTEL_ID, DATE, null);

        assertThat(result).isEmpty();
        verify(repository).findByHotelIdOrderByDateAsc(HOTEL_ID);
    }

    @Test
    void shouldReturnEmptyListWhenNoDocuments() {
        when(repository.findByHotelIdOrderByDateAsc(HOTEL_ID)).thenReturn(List.of());

        List<Availability> result = adapter.findByHotel(HOTEL_ID, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapMultipleDocumentsToDomain() {
        AvailabilityDocument doc1 = AvailabilityDocument.builder()
                .hotelId(HOTEL_ID).date(DATE).occupied(10).capacity(100)
                .status(AvailabilityStatus.AVAILABLE).updatedAt(Instant.now()).build();
        AvailabilityDocument doc2 = AvailabilityDocument.builder()
                .hotelId(HOTEL_ID).date(DATE.plusDays(1)).occupied(100).capacity(100)
                .status(AvailabilityStatus.SOLD_OUT).updatedAt(Instant.now()).build();

        Availability domain1 = new Availability(HOTEL_ID, DATE, 10, 100, AvailabilityStatus.AVAILABLE);
        Availability domain2 = new Availability(HOTEL_ID, DATE.plusDays(1), 100, 100, AvailabilityStatus.SOLD_OUT);

        when(repository.findByHotelIdOrderByDateAsc(HOTEL_ID)).thenReturn(List.of(doc1, doc2));
        when(mapper.toDomain(doc1)).thenReturn(domain1);
        when(mapper.toDomain(doc2)).thenReturn(domain2);

        List<Availability> result = adapter.findByHotel(HOTEL_ID, null, null);

        assertThat(result).containsExactly(domain1, domain2);
    }

    // ── findPagedByHotel ──────────────────────────────────────────────────────

    @Test
    void shouldReturnPagedResultWithTotalElements() {
        LocalDate from = LocalDate.of(2024, 6, 1);
        LocalDate to = LocalDate.of(2024, 6, 7);
        var pageable = PageRequest.of(0, 10);

        AvailabilityDocument doc = AvailabilityDocument.builder()
                .hotelId(HOTEL_ID).date(from).occupied(10).capacity(100)
                .status(AvailabilityStatus.AVAILABLE).updatedAt(Instant.now()).build();
        Availability domain = new Availability(HOTEL_ID, from, 10, 100, AvailabilityStatus.AVAILABLE);

        Page<AvailabilityDocument> page = new PageImpl<>(List.of(doc), pageable, 25L);
        when(repository.findByHotelIdAndDateBetweenOrderByDateAsc(HOTEL_ID, from, to, pageable))
                .thenReturn(page);
        when(mapper.toDomain(doc)).thenReturn(domain);

        PagedResult<Availability> result = adapter.findPagedByHotel(HOTEL_ID, from, to, 0, 10);

        assertThat(result.content()).containsExactly(domain);
        assertThat(result.totalElements()).isEqualTo(25L);
    }

    @Test
    void shouldUseAllDaysPagedQueryWhenDatesAreNull() {
        var pageable = PageRequest.of(0, 30);

        Page<AvailabilityDocument> page = new PageImpl<>(List.of(), pageable, 0L);
        when(repository.findByHotelIdOrderByDateAsc(HOTEL_ID, pageable)).thenReturn(page);

        PagedResult<Availability> result = adapter.findPagedByHotel(HOTEL_ID, null, null, 0, 30);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        verify(repository).findByHotelIdOrderByDateAsc(HOTEL_ID, pageable);
    }

    // ── upsert ───────────────────────────────────────────────────────────────

    @Test
    void shouldCallMongoTemplateUpsertWithCorrectParameters() {
        Availability availability = new Availability(HOTEL_ID, DATE, 50, 100, AvailabilityStatus.AVAILABLE);

        adapter.upsert(availability);

        verify(mongoTemplate).upsert(any(Query.class), any(Update.class), eq(AvailabilityDocument.class));
    }

    @Test
    void shouldBuildCorrectDocumentIdForUpsert() {
        Availability availability = new Availability(HOTEL_ID, DATE, 50, 100, AvailabilityStatus.AVAILABLE);

        adapter.upsert(availability);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).upsert(queryCaptor.capture(), any(Update.class), eq(AvailabilityDocument.class));

        String expectedId = "hotel_1_2024-06-01";
        assertThat(queryCaptor.getValue().getQueryObject().get("_id")).isEqualTo(expectedId);
    }

    // ── bulkUpsert ──────────────────────────────────────────────────────────

    @Test
    void shouldExecuteBulkUpsertWithAllItems() {
        BulkOperations bulkOps = mock(BulkOperations.class);
        when(mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, AvailabilityDocument.class))
                .thenReturn(bulkOps);
        when(bulkOps.upsert(any(Query.class), any(Update.class))).thenReturn(bulkOps);

        List<Availability> items = List.of(
                new Availability(HOTEL_ID, DATE, 10, 100, AvailabilityStatus.AVAILABLE),
                new Availability(HOTEL_ID, DATE.plusDays(1), 90, 100, AvailabilityStatus.LAST_ROOMS)
        );

        adapter.bulkUpsert(items);

        verify(bulkOps, times(2)).upsert(any(Query.class), any(Update.class));
        verify(bulkOps).execute();
    }

    @Test
    void shouldNotExecuteBulkOpsWhenListIsEmpty() {
        adapter.bulkUpsert(List.of());

        verifyNoInteractions(mongoTemplate);
    }
}
