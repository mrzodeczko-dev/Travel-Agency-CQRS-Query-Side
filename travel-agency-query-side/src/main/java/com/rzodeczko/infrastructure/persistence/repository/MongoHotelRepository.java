package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.document.HotelDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoHotelRepository extends MongoRepository<HotelDocument, Long> {
}
