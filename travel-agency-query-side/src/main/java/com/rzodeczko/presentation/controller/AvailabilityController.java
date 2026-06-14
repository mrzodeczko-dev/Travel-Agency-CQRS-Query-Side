package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.port.in.GetAvailabilityUseCase;
import com.rzodeczko.domain.model.Availability;
import com.rzodeczko.presentation.dto.AvailabilityResponseDto;
import com.rzodeczko.presentation.dto.PagedAvailabilityResponseDto;
import com.rzodeczko.presentation.exception.InvalidDateRangeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Hotel room availability queries")
public class AvailabilityController {
    private final GetAvailabilityUseCase getAvailabilityUseCase;

    @Operation(
            summary = "Get hotel availability",
            description = "Returns paginated availability data for a hotel, optionally filtered by date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability data returned"),
            @ApiResponse(responseCode = "400", description = "Invalid date range or parameters")
    })
    @GetMapping("/{hotelId}")
    public ResponseEntity<PagedAvailabilityResponseDto> getAvailability(
            @Parameter(description = "Hotel ID", example = "1")
            @PathVariable long hotelId,
            @Parameter(description = "Start date (inclusive)", example = "2026-07-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive)", example = "2026-07-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size (1-100)", example = "30") @RequestParam(defaultValue = "30") @Range(min = 1, max = 100) int size) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidDateRangeException(from, to);
        }

        List<Availability> result = getAvailabilityUseCase.getForHotel(hotelId, from, to, page, size);
        long totalElements = getAvailabilityUseCase.countForHotel(hotelId, from, to);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<AvailabilityResponseDto> content = result.stream()
                .map(a -> new AvailabilityResponseDto(
                        a.getHotelId(),
                        a.getDate(),
                        a.getOccupied(),
                        a.getCapacity(),
                        a.freeRooms(),
                        a.getStatus()
                )).toList();

        return ResponseEntity.ok(new PagedAvailabilityResponseDto(content, page, size, totalElements, totalPages));
    }
}
