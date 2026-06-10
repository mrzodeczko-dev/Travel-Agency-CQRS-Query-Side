package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface MongoDailyAvailabilityRepository extends MongoRepository<AvailabilityDocument, String> {
    List<AvailabilityDocument> findByHotelIdOrderByDateAsc(long hotelId);

    Window<AvailabilityDocument> findByHotelIdOrderByDateAsc(long hotelId, ScrollPosition position, Limit limit);

    Page<AvailabilityDocument> findByHotelIdOrderByDateAsc(long hotelId, Pageable pageable);

    List<AvailabilityDocument> findByHotelIdAndDateBetweenOrderByDateAsc(long hotelId, LocalDate from, LocalDate to);

    Page<AvailabilityDocument> findByHotelIdAndDateBetweenOrderByDateAsc(long hotelId, LocalDate from, LocalDate to, Pageable pageable);

    long countByHotelId(long hotelId);

    long countByHotelIdAndDateBetween(long hotelId, LocalDate from, LocalDate to);
}
