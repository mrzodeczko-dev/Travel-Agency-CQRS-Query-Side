package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.dto.PagedResult;
import com.rzodeczko.application.port.in.GetAvailabilityUseCase;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/availability")
@Tag(name = "Availability", description = "Hotel room availability queries")
public class AvailabilityController {
    private final GetAvailabilityUseCase getAvailabilityUseCase;

    public AvailabilityController(
            @Qualifier("getAvailabilityUseCase") GetAvailabilityUseCase getAvailabilityUseCase) {
        this.getAvailabilityUseCase = getAvailabilityUseCase;
    }

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

        validateDateRange(from, to);

        var pagedResult = getAvailabilityUseCase.getPagedForHotel(hotelId, from, to, page, size);
        int totalPages = (int) Math.ceil((double) pagedResult.totalElements() / size);

        List<AvailabilityResponseDto> content = pagedResult.content().stream()
                .map(a -> new AvailabilityResponseDto(
                        a.getHotelId(),
                        a.getDate(),
                        a.getOccupied(),
                        a.getCapacity(),
                        a.freeRooms(),
                        a.getStatus().name()
                )).toList();

        return ResponseEntity.ok(new PagedAvailabilityResponseDto(content, page, size, pagedResult.totalElements(), totalPages));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if ((from == null) == (to != null)) {
            throw new InvalidDateRangeException("Both 'from' and 'to' must be provided together, or neither");
        }
        if (from != null && from.isAfter(to)) {
            throw new InvalidDateRangeException(from, to);
        }
    }
}
