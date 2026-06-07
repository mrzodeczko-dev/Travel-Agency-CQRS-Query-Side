package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.DailyAvailability;
import com.rzodeczko.infrastructure.persistence.document.DailyAvailabilityDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DailyAvailabilityDocumentMapper {

    public DailyAvailabilityDocument toDocument(DailyAvailability domain) {
        return DailyAvailabilityDocument.builder()
                .id(DailyAvailabilityDocument.buildId(domain.getHotelId(), domain.getDate()))
                .hotelId(domain.getHotelId())
                .date(domain.getDate())
                .occupied(domain.getOccupied())
                .capacity(domain.getCapacity())
                .status(domain.getStatus())
                .updatedAt(Instant.now())
                .build();
    }

    public DailyAvailability toDomain(DailyAvailabilityDocument doc) {
        return new DailyAvailability(
                doc.getHotelId(),
                doc.getDate(),
                doc.getOccupied(),
                doc.getCapacity(),
                doc.getStatus()
        );
    }
}

