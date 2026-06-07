package com.rzodeczko.application.port.out;


import com.rzodeczko.domain.model.DailyAvailability;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityReadRepository {
    List<DailyAvailability> findByHotel(long hotelId, LocalDate from, LocalDate to);
}
