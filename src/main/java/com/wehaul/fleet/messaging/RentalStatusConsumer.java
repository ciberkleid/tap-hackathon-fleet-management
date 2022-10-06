package com.wehaul.fleet.messaging;

import com.wehaul.fleet.domain.FleetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class RentalStatusConsumer {


    private final FleetService fleetService;

    @Bean
    public Consumer<ReservationChanged> reservationChangeReceived() {
        return reservationChange -> {

            // todo: what if anything this fails

            log.info(" >>> truck reservation change received: [{}]", reservationChange);

            // todo:  confirm event types with the rentals team

            if (reservationChange.getEventType().equals("RESERVATION_PICKED_UP")) {

                fleetService.claimTruck(reservationChange.getTruckId(), "truck rented");
                return;
            }

            if (reservationChange.getEventType().equals("RESERVATION_FULFILLED")) {
                fleetService.releaseTruck(reservationChange.getTruckId(), "truck rental returned", reservationChange.getCurrentMiles());
                return;
            }

            if (reservationChange.getEventType().equals("THE ONE TO IGNORE")) {
                // safely ignoring this
                return;
            }

            log.error("Unexpected event received");
        };
    }
}
