package com.rzodeczko.application.port.in;


import com.rzodeczko.domain.model.DailyAvailability;

import java.time.LocalDate;
import java.util.List;

public interface GetAvailabilityUseCase {
    List<DailyAvailability> getForHotel(long hotelId, LocalDate from, LocalDate to);
}
