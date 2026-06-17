package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.port.in.GetHotelCapacityUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionalGetHotelCapacityUseCaseTest {

    @Mock
    private GetHotelCapacityUseCase delegate;

    private TransactionalGetHotelCapacityUseCase useCase;

    private static final long HOTEL_ID = 1L;
    private static final long CAPACITY = 200L;

    @BeforeEach
    void setUp() {
        useCase = new TransactionalGetHotelCapacityUseCase(delegate);
    }

    @Test
    void getCapacity_delegatesToInner() {
        when(delegate.getCapacity(HOTEL_ID)).thenReturn(OptionalLong.of(CAPACITY));

        useCase.getCapacity(HOTEL_ID);

        verify(delegate, only()).getCapacity(HOTEL_ID);
    }

    @Test
    void getCapacity_returnsResultFromInner() {
        when(delegate.getCapacity(HOTEL_ID)).thenReturn(OptionalLong.of(CAPACITY));

        OptionalLong result = useCase.getCapacity(HOTEL_ID);

        assertThat(result).hasValue(CAPACITY);
    }
}
