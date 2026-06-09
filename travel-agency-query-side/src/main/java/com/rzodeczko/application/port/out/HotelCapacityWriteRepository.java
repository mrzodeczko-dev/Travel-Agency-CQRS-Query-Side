package com.rzodeczko.application.port.out;

public interface HotelCapacityWriteRepository {
    void save(long hotelId, long capacity);
}
