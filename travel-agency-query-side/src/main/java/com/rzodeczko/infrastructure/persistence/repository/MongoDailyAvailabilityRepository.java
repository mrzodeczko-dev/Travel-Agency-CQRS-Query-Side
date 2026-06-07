package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.DailyAvailabilityDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MongoDailyAvailabilityRepository extends MongoRepository<DailyAvailabilityDocument, String> {
    List<DailyAvailabilityDocument> findByHotelIdOrderByDateAsc(long hotelId);

    List<DailyAvailabilityDocument> findByHotelIdAndDateBetweenOrderByDateAsc(long hotelId, LocalDate from, LocalDate to);
}
