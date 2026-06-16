package com.rzodeczko.application.port.out;


import com.rzodeczko.domain.model.Availability;

import java.util.List;

public interface AvailabilityWriteRepository {
    void upsert(Availability availability);

    void bulkUpsert(List<Availability> availabilities);
}
