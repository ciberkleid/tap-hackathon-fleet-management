package com.wehaul.fleet.messaging;

import com.wehaul.fleet.domain.DomainEventPublisher;
import com.wehaul.fleet.domain.Truck;
import com.wehaul.fleet.domain.TruckAvailabilityChanged;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public record NotificationService(StreamBridge streamBridge) implements DomainEventPublisher {

    @Override
    public void publish(TruckAvailabilityChanged truckAvailabilityChanged) {
        streamBridge.send("availability", truckAvailabilityChanged);
    }

    @Bean
    public Consumer<TruckAvailabilityChanged> availabilityReceived() {
        return truckAvailabilityChanged -> log.info(" <<< truck availability change received: [{}]", truckAvailabilityChanged);
    }
}
