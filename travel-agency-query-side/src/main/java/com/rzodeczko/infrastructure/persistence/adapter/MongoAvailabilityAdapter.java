package com.rzodeczko.infrastructure.persistence.adapter;

import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.domain.model.DailyAvailability;
import com.rzodeczko.infrastructure.persistence.document.DailyAvailabilityDocument;
import com.rzodeczko.infrastructure.persistence.mapper.DailyAvailabilityDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoDailyAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MongoAvailabilityAdapter implements
        AvailabilityReadRepository,
        AvailabilityWriteRepository {

    private final MongoTemplate mongoTemplate;
    private final MongoDailyAvailabilityRepository repository;
    private final DailyAvailabilityDocumentMapper mapper;

    @Override
    public List<DailyAvailability> findByHotel(long hotelId, LocalDate from, LocalDate to) {
        List<DailyAvailabilityDocument> docs = from != null && to != null ?
                repository.findByHotelIdAndDateBetweenOrderByDateAsc(hotelId, from, to) :
                repository.findByHotelIdOrderByDateAsc(hotelId);

        return docs.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void upsert(DailyAvailability availability) {
        String id = DailyAvailabilityDocument.buildId(availability.getHotelId(), availability.getDate());
        Query query = Query.query(Criteria.where("_id").is(id));

        Update update = new Update()
                .set("hotelId", availability.getHotelId())
                .set("date", availability.getDate())
                .set("occupied", availability.getOccupied())
                .set("capacity", availability.getCapacity())
                .set("status", availability.getStatus())
                .set("updatedAt", System.currentTimeMillis());

        mongoTemplate.upsert(query, update, DailyAvailabilityDocument.class);
    }
}
