package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityDocumentMapperTest {

    private final AvailabilityDocumentMapper mapper = new AvailabilityDocumentMapper();

    private static final long HOTEL_ID = 42L;
    private static final LocalDate DATE = LocalDate.of(2024, 6, 15);

    // ── toDomain ─────────────────────────────────────────────────────────────

    @Test
    void shouldMapDocumentToDomain() {
        AvailabilityDocument doc = AvailabilityDocument.builder()
                .id("hotel_42_2024-06-15")
                .hotelId(HOTEL_ID)
                .date(DATE)
                .occupied(90)
                .capacity(100)
                .status(AvailabilityStatus.LAST_ROOMS)
                .updatedAt(Instant.now())
                .build();

        Availability domain = mapper.toDomain(doc);

        assertThat(domain.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(domain.getDate()).isEqualTo(DATE);
        assertThat(domain.getOccupied()).isEqualTo(90);
        assertThat(domain.getCapacity()).isEqualTo(100);
        assertThat(domain.getStatus()).isEqualTo(AvailabilityStatus.LAST_ROOMS);
    }

}
