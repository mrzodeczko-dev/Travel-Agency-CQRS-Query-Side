package com.rzodeczko.application.port.out;


import com.rzodeczko.domain.model.DailyAvailability;

public interface AvailabilityWriteRepository {
    void upsert(DailyAvailability availability);
}
