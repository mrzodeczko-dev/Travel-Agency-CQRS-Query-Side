package com.rzodeczko.application.port.out;


import com.rzodeczko.application.dto.PagedResult;
import com.rzodeczko.domain.model.Availability;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public interface AvailabilityReadRepository {
    List<Availability> findByHotel(long hotelId, LocalDate from, LocalDate to);

    PagedResult<Availability> findPagedByHotel(long hotelId, LocalDate from, LocalDate to, int page, int size);

    void forEachByHotel(long hotelId, Consumer<Availability> action);
}
