package com.rzodeczko.application.service;

import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.application.port.out.HotelCapacityWriteRepository;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.domain.model.AvailabilityStatusPolicy;
import com.rzodeczko.domain.model.Availability;

public class HotelCapacityService implements UpsertHotelCapacityUseCase {

    private final HotelCapacityWriteRepository hotelCapacityWriteRepository;
    private final AvailabilityReadRepository availabilityRepository;
    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final AvailabilityStatusPolicy availabilityStatusPolicy;

    public HotelCapacityService(HotelCapacityWriteRepository hotelCapacityWriteRepository, AvailabilityReadRepository availabilityRepository, AvailabilityWriteRepository availabilityWriteRepository, AvailabilityStatusPolicy availabilityStatusPolicy) {
        this.hotelCapacityWriteRepository = hotelCapacityWriteRepository;
        this.availabilityRepository = availabilityRepository;
        this.availabilityWriteRepository = availabilityWriteRepository;
        this.availabilityStatusPolicy = availabilityStatusPolicy;
    }

    @Override
    public void upsert(long hotelId, long capacity) {
        hotelCapacityWriteRepository.save(hotelId, capacity);
        reprojectHotelDays(hotelId, capacity);
    }

    private void reprojectHotelDays(long hotelId, long capacity) {
        availabilityRepository.forEachByHotel(hotelId, day -> {
            AvailabilityStatus newStatus = availabilityStatusPolicy.evaluate(day.getOccupied(), capacity);
            Availability corrected = new Availability(
                    day.getHotelId(),
                    day.getDate(),
                    day.getOccupied(),
                    capacity,
                    newStatus
            );
            availabilityWriteRepository.upsert(corrected);
        });
    }
}
