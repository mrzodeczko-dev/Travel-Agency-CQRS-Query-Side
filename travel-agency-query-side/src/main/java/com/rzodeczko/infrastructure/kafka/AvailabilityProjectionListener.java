package com.rzodeczko.infrastructure.kafka;

import com.rzodeczko.application.command.UpdateAvailabilityCommand;
import com.rzodeczko.application.port.in.UpdateAvailabilityUseCase;
import com.rzodeczko.avro.AvailabilityUpdatedAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AvailabilityProjectionListener {
    private final UpdateAvailabilityUseCase updateAvailabilityUseCase;

    public AvailabilityProjectionListener(
            @Qualifier("retryingUpdateAvailability") UpdateAvailabilityUseCase updateAvailabilityUseCase) {
        this.updateAvailabilityUseCase = updateAvailabilityUseCase;
    }

    @KafkaListener(
            topics = "${app.topics.availability}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onAvailabilityUpdated(AvailabilityUpdatedAvro event) {

        UpdateAvailabilityCommand command = new UpdateAvailabilityCommand(
                event.getHotelId(),
                event.getDate(),
                event.getOccupied()
        );

        updateAvailabilityUseCase.update(command);

        log.debug(
                "Projection upserted hotelId={}, date={}, occupied={}",
                event.getHotelId(),
                event.getDate(),
                event.getOccupied()
        );
    }
}
