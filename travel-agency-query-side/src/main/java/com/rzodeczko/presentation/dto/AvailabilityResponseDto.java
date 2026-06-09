package com.rzodeczko.presentation.dto;

import com.rzodeczko.domain.model.AvailabilityStatus;

import java.time.LocalDate;

public record AvailabilityResponseDto(
        long hotelId,
        LocalDate date,
        long occupied,
        long capacity,
        long freeRooms,
        AvailabilityStatus status
) {
}
