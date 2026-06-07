package com.rzodeczko.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.topics")
public record AppTopicsProperties(
        String bookings,
        String dailyAvailability,
        String dailyAvailabilityDlt,
        String hotels,
        String hotelsDlt
) {
}
