package com.rzodeczko.presentation.controller;

import com.rzodeczko.application.port.in.GetHotelCapacityUseCase;
import com.rzodeczko.presentation.dto.HotelCapacityResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Tag(name = "Hotels", description = "Hotel capacity management")
public class HotelController {

    private final GetHotelCapacityUseCase getHotelCapacityUseCase;

    @Operation(summary = "Get hotel capacity", description = "Returns hotel capacity information by hotel ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel found"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelCapacityResponseDto> getHotel(
            @Parameter(description = "Hotel ID", example = "1") @PathVariable long hotelId) {
        return getHotelCapacityUseCase.getCapacity(hotelId)
                .stream()
                .mapToObj(capacity -> new HotelCapacityResponseDto(hotelId, capacity))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
