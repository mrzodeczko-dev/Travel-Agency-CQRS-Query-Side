package com.rzodeczko.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(AppTopicsProperties.class)
@ConfigurationProperties(prefix = "app.topics")
public record AppTopicsProperties(
        String bookinsgs,
        String dailyAvailability,
        String dailyAvailabilityDlt,
        String hotels,
        String hotelCapacity
) {
}
