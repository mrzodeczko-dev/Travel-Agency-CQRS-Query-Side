package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetryingUpsertHotelCapacityUseCaseTest {

    @Mock
    private UpsertHotelCapacityUseCase delegate;

    private RetryingUpsertHotelCapacityUseCase useCase;

    private static final long HOTEL_ID = 42L;
    private static final long CAPACITY = 300L;

    @BeforeEach
    void setUp() {
        useCase = new RetryingUpsertHotelCapacityUseCase(delegate);
    }

    @Test
    void upsert_delegatesToInner() {
        useCase.upsert(HOTEL_ID, CAPACITY);

        verify(delegate, only()).upsert(HOTEL_ID, CAPACITY);
    }
}
