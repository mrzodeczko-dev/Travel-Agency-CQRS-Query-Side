package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import org.springframework.stereotype.Component;

@Component
public class AvailabilityDocumentMapper {

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

