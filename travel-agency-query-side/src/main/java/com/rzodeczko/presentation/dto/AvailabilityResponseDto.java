package com.rzodeczko.presentation.dto;

import java.time.LocalDate;

public record AvailabilityResponseDto(
        long hotelId,
        LocalDate date,
        long occupied,
        long capacity,
        long freeRooms,
        String status
) {
}
