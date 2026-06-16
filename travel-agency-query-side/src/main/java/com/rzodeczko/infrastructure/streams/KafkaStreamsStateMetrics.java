package com.rzodeczko.infrastructure.streams;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsStateMetrics implements MeterBinder {

    private final StreamsBuilderFactoryBean factoryBean;

    public KafkaStreamsStateMetrics(StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        for (KafkaStreams.State state : KafkaStreams.State.values()) {
            Gauge.builder("kafka.stream.state", () -> currentStateMatches(state) ? 1.0 : 0.0)
                    .tag("state", state.name())
                    .description("KafkaStreams lifecycle state (1 = active, 0 = inactive)")
                    .register(registry);
        }
    }

    private boolean currentStateMatches(KafkaStreams.State expected) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        return kafkaStreams != null && kafkaStreams.state() == expected;
    }
}
