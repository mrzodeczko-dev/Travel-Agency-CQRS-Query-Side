package com.rzodeczko.application.port.in;

public interface UpsertHotelCapacityUseCase {
    void upsert(long hotelId, int capacity);
}
