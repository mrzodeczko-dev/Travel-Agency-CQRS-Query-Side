package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AvailabilityDocumentMapper {

    public AvailabilityDocument toDocument(Availability domain) {
        return AvailabilityDocument.builder()
                .id(AvailabilityDocument.buildId(domain.getHotelId(), domain.getDate()))
                .hotelId(domain.getHotelId())
                .date(domain.getDate())
                .occupied(domain.getOccupied())
                .capacity(domain.getCapacity())
                .status(domain.getStatus())
                .updatedAt(Instant.now())
                .build();
    }

    public Availability toDomain(AvailabilityDocument doc) {
        return new Availability(
                doc.getHotelId(),
                doc.getDate(),
                doc.getOccupied(),
                doc.getCapacity(),
                doc.getStatus()
        );
    }
}

