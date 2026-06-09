package com.rzodeczko.application.port.out;


import com.rzodeczko.domain.model.Availability;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityReadRepository {
    List<Availability> findByHotel(long hotelId, LocalDate from, LocalDate to);
}
