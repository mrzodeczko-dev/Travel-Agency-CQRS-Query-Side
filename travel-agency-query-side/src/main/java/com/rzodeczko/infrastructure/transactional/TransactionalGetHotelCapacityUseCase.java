package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.port.in.GetHotelCapacityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.OptionalLong;

@RequiredArgsConstructor
public class TransactionalGetHotelCapacityUseCase implements GetHotelCapacityUseCase {

    private final GetHotelCapacityUseCase delegate;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OptionalLong getCapacity(long hotelId) {
        return delegate.getCapacity(hotelId);
    }
}
