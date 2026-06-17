package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@RequiredArgsConstructor
public class RetryingUpsertHotelCapacityUseCase implements UpsertHotelCapacityUseCase {

    private final UpsertHotelCapacityUseCase delegate;

    @Override
    @Retryable(
            retryFor = DuplicateKeyException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50))
    public void upsert(long hotelId, long capacity) {
        delegate.upsert(hotelId, capacity);
    }
}
