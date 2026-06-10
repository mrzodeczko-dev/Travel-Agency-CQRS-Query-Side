package com.rzodeczko.infrastructure.persistence.adapter;

import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.infrastructure.persistence.document.AvailabilityDocument;
import com.rzodeczko.infrastructure.persistence.mapper.AvailabilityDocumentMapper;
import com.rzodeczko.infrastructure.persistence.repository.MongoDailyAvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MongoAvailabilityAdapter implements
        AvailabilityReadRepository,
        AvailabilityWriteRepository {

    private final MongoTemplate mongoTemplate;
    private final MongoDailyAvailabilityRepository repository;
    private final AvailabilityDocumentMapper mapper;

    @Override
    public List<Availability> findByHotel(long hotelId, LocalDate from, LocalDate to) {
        List<AvailabilityDocument> docs = from != null && to != null ?
                repository.findByHotelIdAndDateBetweenOrderByDateAsc(hotelId, from, to) :
                repository.findByHotelIdOrderByDateAsc(hotelId);

        return docs.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Availability> findByHotel(long hotelId, LocalDate from, LocalDate to, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var docs = from != null && to != null
                ? repository.findByHotelIdAndDateBetweenOrderByDateAsc(hotelId, from, to, pageable)
                : repository.findByHotelIdOrderByDateAsc(hotelId, pageable);

        return docs.getContent().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long countByHotel(long hotelId, LocalDate from, LocalDate to) {
        return from != null && to != null
                ? repository.countByHotelIdAndDateBetween(hotelId, from, to)
                : repository.countByHotelId(hotelId);
    }

    @Override
    public void forEachByHotel(long hotelId, Consumer<Availability> action) {
        ScrollPosition position = ScrollPosition.keyset();
        boolean hasMore = true;

        while (hasMore) {
            Window<AvailabilityDocument> window = repository.findByHotelIdOrderByDateAsc(
                    hotelId, position, Limit.of(500));

            window.forEach(doc -> action.accept(mapper.toDomain(doc)));

            hasMore = window.hasNext();
            if (hasMore) {
                position = window.positionAt(window.size() - 1);
            }
        }
    }

    @Override
    public void upsert(Availability availability) {
        String id = AvailabilityDocument.buildId(availability.getHotelId(), availability.getDate());
        Query query = Query.query(Criteria.where("_id").is(id));

        Update update = new Update()
                .set("hotelId", availability.getHotelId())
                .set("date", availability.getDate())
                .set("occupied", availability.getOccupied())
                .set("capacity", availability.getCapacity())
                .set("status", availability.getStatus())
                .set("updatedAt", Instant.now());

        mongoTemplate.upsert(query, update, AvailabilityDocument.class);
    }
}
