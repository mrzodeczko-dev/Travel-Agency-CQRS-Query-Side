package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.dto.PagedResult;
import com.rzodeczko.application.port.in.GetAvailabilityUseCase;
import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.domain.model.AvailabilityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionalGetAvailabilityUseCaseTest {

    @Mock
    private GetAvailabilityUseCase delegate;

    private TransactionalGetAvailabilityUseCase useCase;

    private static final long HOTEL_ID = 1L;
    private static final LocalDate FROM = LocalDate.of(2024, 6, 1);
    private static final LocalDate TO = LocalDate.of(2024, 6, 7);
    private static final int PAGE = 0;
    private static final int SIZE = 10;

    @BeforeEach
    void setUp() {
        useCase = new TransactionalGetAvailabilityUseCase(delegate);
    }

    @Test
    void getForHotel_delegatesToInner() {
        when(delegate.getForHotel(HOTEL_ID, FROM, TO)).thenReturn(List.of());

        useCase.getForHotel(HOTEL_ID, FROM, TO);

        verify(delegate).getForHotel(HOTEL_ID, FROM, TO);
    }

    @Test
    void getForHotel_returnsResultFromInner() {
        List<Availability> expected = List.of(
                new Availability(HOTEL_ID, FROM, 10, 100, AvailabilityStatus.AVAILABLE)
        );
        when(delegate.getForHotel(HOTEL_ID, FROM, TO)).thenReturn(expected);

        List<Availability> result = useCase.getForHotel(HOTEL_ID, FROM, TO);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getPagedForHotel_delegatesToInner() {
        when(delegate.getPagedForHotel(HOTEL_ID, FROM, TO, PAGE, SIZE))
                .thenReturn(new PagedResult<>(List.of(), 0L));

        useCase.getPagedForHotel(HOTEL_ID, FROM, TO, PAGE, SIZE);

        verify(delegate, only()).getPagedForHotel(HOTEL_ID, FROM, TO, PAGE, SIZE);
    }

    @Test
    void getPagedForHotel_returnsResultFromInner() {
        List<Availability> content = List.of(
                new Availability(HOTEL_ID, FROM, 5, 100, AvailabilityStatus.AVAILABLE)
        );
        PagedResult<Availability> expected = new PagedResult<>(content, 1L);
        when(delegate.getPagedForHotel(HOTEL_ID, FROM, TO, PAGE, SIZE)).thenReturn(expected);

        PagedResult<Availability> result = useCase.getPagedForHotel(HOTEL_ID, FROM, TO, PAGE, SIZE);

        assertThat(result).isEqualTo(expected);
    }
}
