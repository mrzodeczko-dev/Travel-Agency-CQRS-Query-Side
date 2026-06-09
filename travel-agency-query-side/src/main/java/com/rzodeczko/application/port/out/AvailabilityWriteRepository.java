package com.rzodeczko.application.port.out;


import com.rzodeczko.domain.model.Availability;

public interface AvailabilityWriteRepository {
    void upsert(Availability availability);
}
