package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class TransactionalUpsertHotelCapacityUseCase implements UpsertHotelCapacityUseCase {

    private final UpsertHotelCapacityUseCase delegate;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void upsert(long hotelId, long capacity) {
        delegate.upsert(hotelId, capacity);
    }
}
