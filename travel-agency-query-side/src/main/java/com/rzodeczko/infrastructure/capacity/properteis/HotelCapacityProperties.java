package com.rzodeczko.infrastructure.capacity.properteis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.hotels")
public record HotelCapacityProperties(
        int defaultCapacity,
        Map<Long, Integer> overrides
        ) {
}
