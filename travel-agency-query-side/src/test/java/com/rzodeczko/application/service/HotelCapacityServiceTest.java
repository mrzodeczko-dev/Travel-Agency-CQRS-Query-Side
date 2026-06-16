package com.rzodeczko.application.service;

import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.application.port.out.HotelCapacityReadRepository;
import com.rzodeczko.application.port.out.HotelCapacityWriteRepository;
import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.domain.model.AvailabilityStatus;
import com.rzodeczko.domain.model.AvailabilityStatusPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelCapacityServiceTest {

    @Mock
    private HotelCapacityWriteRepository capacityWriteRepository;
    @Mock
    private HotelCapacityReadRepository capacityReadRepository;
    @Mock
    private AvailabilityReadRepository readRepository;
    @Mock
    private AvailabilityWriteRepository writeRepository;

    private HotelCapacityService service;

    private static final long HOTEL_ID = 1L;

    @BeforeEach
    void setUp() {
        AvailabilityStatusPolicy policy = new AvailabilityStatusPolicy(0.9);
        service = new HotelCapacityService(capacityWriteRepository, capacityReadRepository, readRepository, writeRepository, policy);
    }

    @SuppressWarnings("unchecked")
    private void stubForEachByHotel(long hotelId, List<Availability> days) {
        doAnswer(invocation -> {
            Consumer<Availability> action = invocation.getArgument(1);
            days.forEach(action);
            return null;
        }).when(readRepository).forEachByHotel(eq(hotelId), any(Consumer.class));
    }

    @Test
    void shouldSaveNewCapacity() {
        stubForEachByHotel(HOTEL_ID, List.of());

        service.upsert(HOTEL_ID, 200L);

        verify(capacityWriteRepository).save(HOTEL_ID, 200L);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReprojectExistingDaysWithNewCapacity() {
        LocalDate date = LocalDate.of(2024, 6, 1);
        Availability existing = new Availability(HOTEL_ID, date, 50, 100, AvailabilityStatus.AVAILABLE);
        stubForEachByHotel(HOTEL_ID, List.of(existing));

        service.upsert(HOTEL_ID, 200L);

        ArgumentCaptor<List<Availability>> captor = ArgumentCaptor.forClass(List.class);
        verify(writeRepository).bulkUpsert(captor.capture());

        Availability reprojected = captor.getValue().getFirst();
        assertThat(reprojected.getCapacity()).isEqualTo(200L);
        assertThat(reprojected.getOccupied()).isEqualTo(50);
        assertThat(reprojected.getHotelId()).isEqualTo(HOTEL_ID);
        assertThat(reprojected.getDate()).isEqualTo(date);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReprojectStatusBasedOnNewCapacity() {
        LocalDate date = LocalDate.of(2024, 6, 1);
        // occupied=90 przy capacity=100 → LAST_ROOMS, ale przy capacity=200 → AVAILABLE
        Availability existing = new Availability(HOTEL_ID, date, 90, 100, AvailabilityStatus.LAST_ROOMS);
        stubForEachByHotel(HOTEL_ID, List.of(existing));

        service.upsert(HOTEL_ID, 200L);

        ArgumentCaptor<List<Availability>> captor = ArgumentCaptor.forClass(List.class);
        verify(writeRepository).bulkUpsert(captor.capture());

        assertThat(captor.getValue().getFirst().getStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReprojectAllDaysForHotelInSingleBulkUpsert() {
        List<Availability> days = List.of(
                new Availability(HOTEL_ID, LocalDate.of(2024, 6, 1), 10, 100, AvailabilityStatus.AVAILABLE),
                new Availability(HOTEL_ID, LocalDate.of(2024, 6, 2), 20, 100, AvailabilityStatus.AVAILABLE),
                new Availability(HOTEL_ID, LocalDate.of(2024, 6, 3), 30, 100, AvailabilityStatus.AVAILABLE)
        );
        stubForEachByHotel(HOTEL_ID, days);

        service.upsert(HOTEL_ID, 150L);

        ArgumentCaptor<List<Availability>> captor = ArgumentCaptor.forClass(List.class);
        verify(writeRepository).bulkUpsert(captor.capture());

        assertThat(captor.getValue()).hasSize(3);
    }

    @Test
    void getCapacity_existingHotel_returnsCapacity() {
        when(capacityReadRepository.findCapacity(HOTEL_ID)).thenReturn(OptionalLong.of(200L));

        OptionalLong result = service.getCapacity(HOTEL_ID);

        assertThat(result).isPresent();
        assertThat(result.getAsLong()).isEqualTo(200L);
    }

    @Test
    void getCapacity_nonExistentHotel_returnsEmpty() {
        when(capacityReadRepository.findCapacity(999L)).thenReturn(OptionalLong.empty());

        OptionalLong result = service.getCapacity(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotCallBulkUpsertWhenHotelHasNoDays() {
        stubForEachByHotel(HOTEL_ID, List.of());

        service.upsert(HOTEL_ID, 100L);

        verify(writeRepository, never()).bulkUpsert(any());
    }
}
