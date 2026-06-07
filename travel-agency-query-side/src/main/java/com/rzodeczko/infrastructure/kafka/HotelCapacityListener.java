package com.rzodeczko.infrastructure.kafka;

import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import com.rzodeczko.avro.HotelUpsertedAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HotelCapacityListener {
    private final UpsertHotelCapacityUseCase upsertHotelCapacityUseCase;

    @KafkaListener(
            topics = "${app.topics.hotels}",
            groupId = "${app.hotels.consumer-group}"
    )
    public void onHotelUpserted(HotelUpsertedAvro event) {
        upsertHotelCapacityUseCase.upsert(
                event.getHotelId(),
                event.getCapacity()
        );
        log.debug(
                "Hotel capacity upserted hotelId={}, capacity={}",
                event.getHotelId(),
                event.getCapacity()
        );
    }
}
