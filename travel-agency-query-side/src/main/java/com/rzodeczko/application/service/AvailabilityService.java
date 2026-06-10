package com.rzodeczko.application.service;

import com.rzodeczko.application.command.UpdateAvailabilityCommand;
import com.rzodeczko.application.port.in.GetAvailabilityUseCase;
import com.rzodeczko.application.port.in.UpdateAvailabilityUseCase;
import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.application.port.out.HotelCapacityProvider;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.domain.model.AvailabilityStatusPolicy;
import com.rzodeczko.domain.model.Availability;

import java.time.LocalDate;
import java.util.List;

public class AvailabilityService implements UpdateAvailabilityUseCase, GetAvailabilityUseCase {

    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final AvailabilityReadRepository availabilityReadRepository;
    private final HotelCapacityProvider hotelCapacityProvider;
    private final AvailabilityStatusPolicy availabilityStatusPolicy;

    public AvailabilityService(
            AvailabilityWriteRepository availabilityWriteRepository,
            AvailabilityReadRepository availabilityReadRepository,
            HotelCapacityProvider hotelCapacityProvider,
            AvailabilityStatusPolicy availabilityStatusPolicy) {
        this.availabilityWriteRepository = availabilityWriteRepository;
        this.availabilityReadRepository = availabilityReadRepository;
        this.hotelCapacityProvider = hotelCapacityProvider;
        this.availabilityStatusPolicy = availabilityStatusPolicy;
    }

    @Override
    public void update(UpdateAvailabilityCommand command) {
        long capacity = hotelCapacityProvider.getCapacity(command.hotelId());

        AvailabilityStatus status = availabilityStatusPolicy.evaluate(command.occupied(), capacity);

        Availability availability = new Availability(
                command.hotelId(),
                command.date(),
                command.occupied(),
                capacity,
                status
        );

        availabilityWriteRepository.upsert(availability);
    }

    @Override
    public List<Availability> getForHotel(long hotelId, LocalDate from, LocalDate to) {
        return availabilityReadRepository.findByHotel(hotelId, from, to);
    }

    @Override
    public List<Availability> getForHotel(long hotelId, LocalDate from, LocalDate to, int page, int size) {
        return availabilityReadRepository.findByHotel(hotelId, from, to, page, size);
    }

    @Override
    public long countForHotel(long hotelId, LocalDate from, LocalDate to) {
        return availabilityReadRepository.countByHotel(hotelId, from, to);
    }
}
