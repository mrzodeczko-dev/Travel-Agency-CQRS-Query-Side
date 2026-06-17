package com.rzodeczko.infrastructure.configuration;

import com.rzodeczko.application.port.in.GetAvailabilityUseCase;
import com.rzodeczko.application.port.in.GetHotelCapacityUseCase;
import com.rzodeczko.application.port.in.UpdateAvailabilityUseCase;
import com.rzodeczko.application.port.in.UpsertHotelCapacityUseCase;
import com.rzodeczko.application.port.out.AvailabilityReadRepository;
import com.rzodeczko.application.port.out.AvailabilityWriteRepository;
import com.rzodeczko.application.port.out.HotelCapacityProvider;
import com.rzodeczko.application.port.out.HotelCapacityReadRepository;
import com.rzodeczko.application.port.out.HotelCapacityWriteRepository;
import com.rzodeczko.application.service.AvailabilityService;
import com.rzodeczko.application.service.HotelCapacityService;
import com.rzodeczko.domain.model.AvailabilityStatusPolicy;
import com.rzodeczko.infrastructure.transactional.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfiguration {

    @Bean
    public AvailabilityStatusPolicy availabilityStatusPolicy(@Value("${app.last-rooms-threshold:0.9}") double lastRoomsThreshold) {
        return new AvailabilityStatusPolicy(lastRoomsThreshold);
    }

    // --- Services ---

    @Bean
    @Qualifier("availabilityService")
    public AvailabilityService availabilityService(
            AvailabilityWriteRepository writeRepository,
            AvailabilityReadRepository readRepository,
            HotelCapacityProvider hotelCapacityProvider,
            AvailabilityStatusPolicy statusPolicy
    ) {
        return new AvailabilityService(
                writeRepository,
                readRepository,
                hotelCapacityProvider,
                statusPolicy);
    }

    @Bean
    @Qualifier("hotelCapacityService")
    public HotelCapacityService hotelCapacityService(
            HotelCapacityWriteRepository capacityWriteRepository,
            HotelCapacityReadRepository capacityReadRepository,
            AvailabilityReadRepository readRepository,
            AvailabilityWriteRepository writeRepository,
            AvailabilityStatusPolicy availabilityStatusPolicy
    ) {
        return new HotelCapacityService(
                capacityWriteRepository,
                capacityReadRepository,
                readRepository,
                writeRepository,
                availabilityStatusPolicy);
    }


    @Bean
    @Qualifier("getAvailabilityUseCase")
    public GetAvailabilityUseCase getAvailabilityUseCase(
            @Qualifier("availabilityService") AvailabilityService availabilityService) {
        return new TransactionalGetAvailabilityUseCase(availabilityService);
    }


    @Bean
    @Qualifier("transactionalUpdateAvailability")
    public UpdateAvailabilityUseCase transactionalUpdateAvailability(
            @Qualifier("availabilityService") AvailabilityService availabilityService) {
        return new TransactionalUpdateAvailabilityUseCase(availabilityService);
    }

    @Bean
    @Qualifier("retryingUpdateAvailability")
    public UpdateAvailabilityUseCase updateAvailabilityUseCase(
            @Qualifier("transactionalUpdateAvailability") UpdateAvailabilityUseCase transactional) {
        return new RetryingUpdateAvailabilityUseCase(transactional);
    }


    @Bean
    @Qualifier("getHotelCapacityUseCase")
    public GetHotelCapacityUseCase getHotelCapacityUseCase(
            @Qualifier("hotelCapacityService") HotelCapacityService hotelCapacityService) {
        return new TransactionalGetHotelCapacityUseCase(hotelCapacityService);
    }


    @Bean
    @Qualifier("transactionalUpsertHotelCapacity")
    public UpsertHotelCapacityUseCase transactionalUpsertHotelCapacity(
            @Qualifier("hotelCapacityService") HotelCapacityService hotelCapacityService) {
        return new TransactionalUpsertHotelCapacityUseCase(hotelCapacityService);
    }

    @Bean
    @Qualifier("retryingUpsertHotelCapacity")
    public UpsertHotelCapacityUseCase upsertHotelCapacityUseCase(
            @Qualifier("transactionalUpsertHotelCapacity") UpsertHotelCapacityUseCase transactional) {
        return new RetryingUpsertHotelCapacityUseCase(transactional);
    }
}
