package com.rzodeczko.infrastructure.persistence.document;

import com.rzodeczko.domain.model.AvailabilityStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Document(collation = "daily_availability")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyAvailabilityDocument {

    @Id
    private String id;

    @Indexed
    private long hotelId;

    private LocalDate date;

    private long occupied;
    private long capacity;
    private AvailabilityStatus status;

    private Instant updatedAt;

    public static String buildId(long hotelId, LocalDate date) {
        return "hotel_" + hotelId + "_" + date.toString();
    }
}
