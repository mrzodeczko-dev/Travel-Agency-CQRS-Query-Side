package com.rzodeczko.infrastructure.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerConfigTest {

    @Mock
    private KafkaTemplate<String, byte[]> dltKafkaTemplate;

    private KafkaConsumerConfig config;

    @BeforeEach
    void setUp() {
        config = new KafkaConsumerConfig();
    }

    @Test
    void errorHandler_returnsNonNullHandler() {
        DefaultErrorHandler handler = config.errorHandler(dltKafkaTemplate);

        assertThat(handler).isNotNull();
    }

    @Test
    void errorHandler_isInstanceOfDefaultErrorHandler() {
        DefaultErrorHandler handler = config.errorHandler(dltKafkaTemplate);

        assertThat(handler).isInstanceOf(DefaultErrorHandler.class);
    }
}
