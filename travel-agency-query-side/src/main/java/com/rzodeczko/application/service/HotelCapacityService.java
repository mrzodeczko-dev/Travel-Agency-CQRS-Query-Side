package com.rzodeczko.application.service;

import com.rzodeczko.application.port.in.GetHotelCapacityUseCase;
import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.application.port.out.HotelCapacityReadRepository;
import com.rzodeczko.application.port.out.HotelCapacityWriteRepository;
import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.domain.model.AvailabilityStatusPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

public class HotelCapacityService implements UpsertHotelCapacityUseCase, GetHotelCapacityUseCase {

    private final HotelCapacityWriteRepository hotelCapacityWriteRepository;
    private final HotelCapacityReadRepository hotelCapacityReadRepository;
    private final AvailabilityReadRepository availabilityRepository;
    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final AvailabilityStatusPolicy availabilityStatusPolicy;
    private static final int BULK_BATCH_SIZE = 500;

    public HotelCapacityService(
            HotelCapacityWriteRepository hotelCapacityWriteRepository,
            HotelCapacityReadRepository hotelCapacityReadRepository,
            AvailabilityReadRepository availabilityRepository,
            AvailabilityWriteRepository availabilityWriteRepository,
            AvailabilityStatusPolicy availabilityStatusPolicy) {
        this.hotelCapacityWriteRepository = hotelCapacityWriteRepository;
        this.hotelCapacityReadRepository = hotelCapacityReadRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityWriteRepository = availabilityWriteRepository;
        this.availabilityStatusPolicy = availabilityStatusPolicy;
    }

    @Override
    public OptionalLong getCapacity(long hotelId) {
        return hotelCapacityReadRepository.findCapacity(hotelId);
    }

    @Override
    public void upsert(long hotelId, long capacity) {
        hotelCapacityWriteRepository.save(hotelId, capacity);
        reprojectHotelDays(hotelId, capacity);
    }

    private void reprojectHotelDays(long hotelId, long capacity) {
        List<Availability> batch = new ArrayList<>(BULK_BATCH_SIZE);

        availabilityRepository.forEachByHotel(hotelId, day -> {
            AvailabilityStatus newStatus = availabilityStatusPolicy.evaluate(day.getOccupied(), capacity);
            batch.add(new Availability(
                    day.getHotelId(),
                    day.getDate(),
                    day.getOccupied(),
                    capacity,
                    newStatus
            ));

            if (batch.size() >= BULK_BATCH_SIZE) {
                availabilityWriteRepository.bulkUpsert(batch);
                batch.clear();
            }
        });

        if (!batch.isEmpty()) {
            availabilityWriteRepository.bulkUpsert(batch);
        }
    }
}
