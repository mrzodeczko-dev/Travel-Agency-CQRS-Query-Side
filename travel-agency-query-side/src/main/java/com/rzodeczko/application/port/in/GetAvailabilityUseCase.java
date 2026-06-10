package com.rzodeczko.application.port.in;


import com.rzodeczko.domain.model.Availability;

import java.time.LocalDate;
import java.util.List;

public interface GetAvailabilityUseCase {
    List<Availability> getForHotel(long hotelId, LocalDate from, LocalDate to);

    List<Availability> getForHotel(long hotelId, LocalDate from, LocalDate to, int page, int size);

    long countForHotel(long hotelId, LocalDate from, LocalDate to);
}
