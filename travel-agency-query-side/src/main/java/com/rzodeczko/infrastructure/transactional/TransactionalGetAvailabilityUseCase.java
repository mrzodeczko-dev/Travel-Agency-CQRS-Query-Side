package com.rzodeczko.infrastructure.transactional;

import com.rzodeczko.application.dto.PagedResult;
import com.rzodeczko.application.port.in.GetAvailabilityUseCase;
import com.rzodeczko.domain.model.Availability;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class TransactionalGetAvailabilityUseCase implements GetAvailabilityUseCase {

    private final GetAvailabilityUseCase delegate;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Availability> getForHotel(long hotelId, LocalDate from, LocalDate to) {
        return delegate.getForHotel(hotelId, from, to);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PagedResult<Availability> getPagedForHotel(long hotelId, LocalDate from, LocalDate to, int page, int size) {
        return delegate.getPagedForHotel(hotelId, from, to, page, size);
    }
}
