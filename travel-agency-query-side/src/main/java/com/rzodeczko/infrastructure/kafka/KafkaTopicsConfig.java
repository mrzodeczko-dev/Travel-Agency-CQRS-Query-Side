package com.rzodeczko.infrastructure.kafka;

import com.rzodeczko.infrastructure.configuration.properties.AppTopicsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfig {
    private final AppTopicsProperties topics;

    @Bean
    public NewTopic dailyAvailabilityTopic() {
        return TopicBuilder
                .name(topics.dailyAvailability())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dailyAvailabilityDltTopic() {
        return TopicBuilder
                .name(topics.dailyAvailabilityDlt())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic hotelsDltTopic() {
        return TopicBuilder
                .name(topics.hotelsDlt())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
