package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MongoDailyAvailabilityRepository extends MongoRepository<AvailabilityDocument, String> {
    List<AvailabilityDocument> findByHotelIdOrderByDateAsc(long hotelId);

    List<AvailabilityDocument> findByHotelIdAndDateBetweenOrderByDateAsc(long hotelId, LocalDate from, LocalDate to);
}
