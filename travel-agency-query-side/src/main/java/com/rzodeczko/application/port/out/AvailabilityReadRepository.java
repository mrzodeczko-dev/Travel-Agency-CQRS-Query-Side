package com.rzodeczko.application.port.out;


import com.rzodeczko.domain.model.Availability;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public interface AvailabilityReadRepository {
    List<Availability> findByHotel(long hotelId, LocalDate from, LocalDate to);

    List<Availability> findByHotel(long hotelId, LocalDate from, LocalDate to, int page, int size);

    long countByHotel(long hotelId, LocalDate from, LocalDate to);

    void forEachByHotel(long hotelId, Consumer<Availability> action);
}
